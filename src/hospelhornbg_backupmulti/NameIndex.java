package hospelhornbg_backupmulti;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.SerializedString;

public class NameIndex {
	
	public static final short VERSION = 1;
	
	private Map<String, List<Long>> map; //Eh.
	
	public NameIndex(String srcpath) throws IOException{
		map = new HashMap<String, List<Long>>();
		if(FileBuffer.fileExists(srcpath)){
			FileBuffer file = FileBuffer.createBuffer(srcpath, true);
			long cpos = 4; //skip header
			long fsz = file.getFileSize();
			
			while(cpos < fsz){
				SerializedString ss = file.readVariableLengthString("UTF8", cpos, BinFieldSize.WORD, 2);
				String s = ss.getString();
				cpos += ss.getSizeOnDisk();
				
				int count = file.intFromFile(cpos); cpos+=4;
				if(count < 1) continue;
				List<Long> l = new LinkedList<Long>();
				map.put(s, l);
				for(int i = 0; i < count; i++){
					l.add(file.longFromFile(cpos));
					cpos+=8;
				}
			}	
		}	
	}

	public Map<String, Collection<Long>> searchByName(String search, boolean caseSensitive, boolean exact){
		List<String> keylist = new LinkedList<String>();
		keylist.addAll(map.keySet());
		Collections.sort(keylist);
		Map<String, Collection<Long>> outmap = new HashMap<String, Collection<Long>>();
		if(search == null) return outmap;
		
		for(String k : keylist){
			String check_k = k;
			String check_q = search;
			if(!caseSensitive){
				check_k = check_k.toLowerCase();
				check_q = check_q.toLowerCase();
			}
			boolean match = false;
			if(exact) match = check_k.equals(check_q);
			else match = check_k.contains(check_q);
			
			if(match){
				List<Long> val = map.get(k);
				List<Long> nlist = new LinkedList<Long>();
				nlist.addAll(val);
				outmap.put(k, nlist);
			}
		}
		
		return outmap;
	}
	
	public void addMapping(String name, long uid){
		List<Long> list = map.get(name);
		if(list == null){
			list = new LinkedList<Long>();
			map.put(name, list);
		}
		list.add(uid);
	}
	
	public void saveIndexTo(String path) throws IOException{
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
		FileBuffer buff = new FileBuffer(8, true);
		buff.addToFile(VERSION);
		buff.addToFile((short)0);
		buff.writeToStream(bos);
		
		List<String> keylist = new LinkedList<String>();
		keylist.addAll(map.keySet());
		Collections.sort(keylist);
		for(String k : keylist){
			List<Long> ids = map.get(k);
			int alloc = (k.length() << 2) + 3 + 4 + (ids.size() << 3);
			buff = new FileBuffer(alloc, true);
			buff.addVariableLengthString("UTF8", k, BinFieldSize.WORD, 2);
			buff.addToFile(ids.size());
			for(Long l : ids) buff.addToFile(l);
			buff.writeToStream(bos);
		}
		
		bos.close();
	}
	
}
