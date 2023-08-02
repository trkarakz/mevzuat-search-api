package com.evrim.mevzuat.search.api.service;

import java.io.File;
import java.util.List;

public interface MevzuatFileListService {
	public List<File> listAllMevzuatFiles();
	public List<File> listChangedMevzuatFiles();
	public File getMevzuatFile(int itemNo);
}
