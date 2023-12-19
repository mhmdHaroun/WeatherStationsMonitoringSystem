package com.kafka.consumer;



import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;



public class BitcaskProxy {
	private Bitcask bitcask;
	private ReadWriteLock lock;
	public BitcaskProxy(long maxSize) {
		bitcask = new Bitcask(maxSize);
		lock = new ReentrantReadWriteLock(); 
    	File bitcaskDir = new File(System.getProperty("user.dir")+"/Bitcask");
    	if(!bitcaskDir.exists()) bitcaskDir.mkdir();
	}
    
	public void write(WeatherStatus weatherStatus){
	    try {
        	lock.writeLock().lock();
        	try {
        		if(weatherStatus != null)
        			bitcask.write(weatherStatus, "Bitcask/segment");
			} finally {
				lock.writeLock().unlock();
			}	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
    
	public void read(int numThreads, final String key) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            Runnable reader = new Runnable() {
				public void run() {
				    try {
				        while (true) {
				        	lock.readLock().lock();
				        	try {
				        		WeatherStatus weatherStatus=bitcask.read(key);
					        	//if(weatherStatus != null) System.out.println(weatherStatus);
					        	//else System.out.println("Key not exists");
							} finally {
								lock.readLock().unlock();
							}
				        	TimeUnit.SECONDS.sleep(1);
				        }
				    } catch (Exception e) {
				        e.printStackTrace();
				    }
				}
			};
            executor.execute(reader);
        }
	}
	
	private File[] getFiles(final String fileName) {
        File[] files =  new File(System.getProperty("user.dir")+"/Bitcask").listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith(fileName);
			}
		});
        return files;
	}
	
	public void compact(final int maxNumFiles) {
        Runnable compactor = new Runnable() {
			public void run() {
			    try {
			    	while(true) {			            
			            lock.readLock().lock();
			            try {
			            	File[]files = getFiles("segment");
				            if(files != null && files.length > maxNumFiles) {
								final String[]fileNames = new String[files.length];
					            for (int i = 0; i < files.length; i++) 
					                fileNames[i] = "Bitcask/"+files[i].getName();
					            bitcask.compaction(fileNames, "Bitcask/compact", "Bitcask/hint");	
				            }
						} finally {
							lock.readLock().unlock();
						}			            			           			            
			            TimeUnit.SECONDS.sleep(10);
		            }
			    } catch (Exception e) {
			        e.printStackTrace();
			    }
			}
		};
		new Thread(compactor).start();
	}
	
	public void crashRecovery(int maxNumFiles) {
		File[]files = getFiles("hint");			            
        if(files != null && files.length > maxNumFiles) {
			String[]fileNames = new String[files.length];
            for (int i = 0; i < files.length; i++) 
                fileNames[i] = "Bitcask/"+files[i].getName();
            try {
				bitcask.crashRecovery(fileNames);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }	
	}

}
