package hospelhornbg_backupmulti;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import waffleoRai_Utils.FileBuffer;

public class DataIndex {
	
	public static final int FILE_VERSION = 1;
	
	public static final int DEFO_REC_LIMIT = 1000000;
	public static final long BLOCK_SIZE = 0x10000;
	
	private Map<Long, DataFile> cache;
	private LinkedList<Long> queue;
	private Set<Long> dirty; //GUIDs of dirty records (must be updated on disc before cleared from mem)
	private long[] index; //First GUID of each block
	
	private String src_path;
	private int limit; //Max records in memory
	
	public DataIndex(String path) throws IOException{
		limit = DEFO_REC_LIMIT;
		src_path = path;
		cache = new TreeMap<Long, DataFile>();
		queue = new LinkedList<Long>();
		dirty = new TreeSet<Long>();
		indexFile();
	}
	
	protected void indexFile() throws IOException{
		//For uniformity, every block has a copy of the file version and record count
		if(!FileBuffer.fileExists(src_path)) return;
		long size = FileBuffer.fileSize(src_path);
		long mod = size % BLOCK_SIZE;
		size += mod;
		int blockcount = (int)(size/BLOCK_SIZE);
		
		index = new long[blockcount];
		FileBuffer buff = FileBuffer.createBuffer(src_path, true);
		long cpos = 0L;
		long off = 8 + 6; //version, record count, first rec flags, size
		for(int i = 0; i < blockcount; i++){
			index[i] = buff.longFromFile(cpos + off);
			cpos += BLOCK_SIZE;
		}
	}
	
	protected DataFile getFromDisc(long uid) throws IOException{
		if(index == null) return null; //Assumed file does not exist.
		
		//Determine which block it's in
		int min = 0;
		int max = index.length;
		int n = index.length/2;
		int b = -1;
		while(b < 0){
			if(uid == index[n]){
				b = n;
				break;
			}
			else if(uid < index[n]){
				//Check earlier block.
				if(n == 0) break;
				max = n;
				n = ((max-min)/2) + min;
			}
			else{
				//In this block?
				if(n >= index.length-1){
					b = index.length - 1;
					break;
				}
				if(uid > index[n+1]){
					b = n;
					break;
				}
				
				//If not, check later block
				min = n+1;
				n = ((max-min)/2) + min;
			}
		}
		if(b < 0) return null;
		
		//Scan block linearly
		FileBuffer file = FileBuffer.createBuffer(src_path, true);
		long boff = (long)b * BLOCK_SIZE;
		long cpos = boff + 8L;
		boff = Math.min(boff + BLOCK_SIZE,file.getFileSize()); //End
		while(cpos < boff){
			long id = file.longFromFile(cpos + 6L);
			if(id != uid){
				cpos += 6L + Integer.toUnsignedLong(file.intFromFile(cpos + 2L));
			}
		}
		if(cpos >= boff) return null;
		
		//Load and return
		DataFile df = DataFile.parseDataFileRecord(file, cpos);
		
		return df;
	}
	
	protected boolean loadFromDisc(long uid) throws IOException{
		//Is cache full?
		if(cache.size() >= limit){
			//Can we pop the oldest from cache to make room?
			//If oldest is dirty, run a save.
			//Pop oldest
			long oldest = queue.poll();
			if(dirty.contains(oldest)) saveDataRecords();
			cache.remove(oldest);
		}
		
		//Call getFromDisc() on new record
		DataFile df = getFromDisc(uid);
		if(df == null) return false;
		
		//Add to cache & queue
		cache.put(uid, df);
		queue.add(uid);
		
		return true;
	}

	public boolean recordExists(long uid){
		//This does not add to dirty set
		if(cache.containsKey(uid)) return true;
		try{return loadFromDisc(uid);}
		catch(Exception ex){ex.printStackTrace();}
		return false;
	}
	
	public DataFile getDataRecord(long uid){
		//Automatically adds to dirty set
		if(!cache.containsKey(uid)){
			try{
				if(!loadFromDisc(uid))return null;
			}
			catch(Exception ex){ex.printStackTrace(); return null;}
		}
		else{
			//Move to queue back
			queue.remove(uid);
			queue.add(uid);
		}
		DataFile df = cache.get(uid);
		if(df == null) return null;
		dirty.add(uid);
		return df;
	}
	
	public int countRecords(){
		if(!FileBuffer.fileExists(src_path)) return cache.size();
		
		Set<Long> newrecs = new TreeSet<Long>();
		newrecs.addAll(dirty);
		
		try{
			FileBuffer in = FileBuffer.createBuffer(src_path, true);
			int count = 0;
			long cpos = 8L;
			long sz = in.getFileSize();
			while(cpos < sz){
				if(cpos % BLOCK_SIZE == 0) cpos += 8;
				long id = in.longFromFile(cpos+6);
				long skip = Integer.toUnsignedLong(in.intFromFile(cpos+2)) + 6;
				newrecs.remove(id);
				count++;
				cpos += skip;
			}
			count += newrecs.size();
			return count;
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
		return 0;
	}
	
	public void addDataRecord(DataFile record){
		if(cache.size() >= limit){
			long oldest = queue.poll();
			if(dirty.contains(oldest)) {
				try{saveDataRecords();}
				catch(IOException ex){
					ex.printStackTrace();
				}
			}
			cache.remove(oldest);
		}
		long id = record.getGUID();
		cache.put(id, record);
		queue.add(id);
		dirty.add(id);
	}
	
	public void saveDataRecords() throws IOException{
		int rcount = countRecords();
		
		String temppath = src_path + ".tmp";
		FileBuffer input = null;
		//int ob = 0; //current output block
		long bpos = 0L; //Position in block
		long inpos = 0L;
		long insz = 0L;
		
		if(FileBuffer.fileExists(src_path)){
			input = FileBuffer.createBuffer(src_path, true);
			inpos = 8L;
			insz = input.getFileSize();
		}
		LinkedList<Long> drecs = new LinkedList<Long>();
		drecs.addAll(dirty);
		Collections.sort(drecs);
		
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(temppath));
		
		//Block 0 header
		FileBuffer buff = new FileBuffer(8, true);
		buff.addToFile(FILE_VERSION);
		buff.addToFile(rcount);
		buff.writeToStream(bos);
		buff = null; bpos = 8;
		
		FileBuffer lastrec = null;
		while(!drecs.isEmpty() || (inpos < insz)){
			FileBuffer newrec = null;
			if(input != null){
				long newid = 0L;
				long oldid = 0L;
				if(!drecs.isEmpty()) newid = drecs.peek();
				if(inpos < insz) oldid = input.longFromFile(inpos + 6);
				if(newid == 0L || (newid != 0L && oldid < newid)){
					//Copy old record
					//If it's the last in the block, reread and serialize it.
					long skip = Integer.toUnsignedLong(input.intFromFile(inpos+2)) + 6;
					long ed = inpos + skip;
					if(ed % BLOCK_SIZE == 0){
						DataFile df = DataFile.parseDataFileRecord(input, inpos);
						newrec = df.serializeMe();
					}
					else{
						newrec = new FileBuffer((int)skip, true);
						for(int i = 0; i < skip; i++) newrec.addToFile(input.getByte(inpos+i));
					}
					inpos = ed;
				}
				else{
					if(newid != 0L && newid == oldid){
						//Move inpos forward, but only add new record
						inpos += Integer.toUnsignedLong(input.intFromFile(inpos+2)) + 6;
					}
					DataFile df = cache.get(newid);
					newrec = df.serializeMe();
					drecs.pop();
				}
			
			}
			else{
				//There aren't any old records. So it's just the next new record.
				long id = drecs.pop();
				DataFile df = cache.get(id);
				newrec = df.serializeMe();
			}
			
			if(newrec == null)break;
			//Room in this block for a new record?
			long space = BLOCK_SIZE - bpos;
			if(newrec.getFileSize() > space){
				if(lastrec != null){
					int sz = lastrec.intFromFile(2);
					sz += (int)space;
					lastrec.replaceInt(sz, 2L);
					lastrec.writeToStream(bos);
				}
				
				//New block.
				buff = new FileBuffer((int)space + 8, true);
				for(int i = 0; i < space; i++) buff.addToFile((byte)0x00);
				buff.addToFile(FILE_VERSION);
				buff.addToFile(rcount);
				buff.writeToStream(bos);
				bpos = 0; buff = null;
			}
			else{
				if(lastrec != null) lastrec.writeToStream(bos);
			}
			//newrec.writeToStream(bos);
			bpos += newrec.getFileSize();
			lastrec = newrec;
		}
		
		//Close stream
		if(lastrec != null) lastrec.writeToStream(bos);
		bos.close();
		dirty.clear();
		
		//Replace old file
		Files.deleteIfExists(Paths.get(src_path));
		Files.move(Paths.get(temppath), Paths.get(src_path));
		indexFile();
	}
	
}
