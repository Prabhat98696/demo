package com.agron.wc.integration.straub.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.util.ArrayList;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;

public class AgronSFTPServerConfig {

	static Channel channel = null;
	//static String ip="12.186.41.135";
	
	static String ip= LCSProperties.get("com.agron.wc.integration.straub.util.sftpRemote.host");
	static String user=LCSProperties.get("com.agron.wc.integration.straub.util.sftpRemote.user");
	static String password= LCSProperties.get("com.agron.wc.integration.straub.util.sftpRemote.password");
	static String remoteDir =LCSProperties.get("com.agron.wc.integration.straub.util.sftpRemote.dir");
	public static String INBOUND_DIR = LCSProperties.get("com.agron.wc.integration.straub.inbound.upload.fileDirPath");
	static Session session = null;
	static SftpATTRS attrs=null;
	
	private static final Logger logger = LogManager.getLogger("com.agron.wc.integration.straub.inbound");


	public static Channel getSFTPChannel() throws JSchException{

		if(channel == null){
			Session session = getSFTPSession();
			if(session!= null){
				channel = session.openChannel("sftp");
				if(channel!= null){
					channel.connect();
					logger.info("!!!!!! SFTP channel >>>>"+channel);
				}
			}
		}
		return channel;
	}

	/*
	 * 
	 * 
	 */
	public static Session getSFTPSession() throws JSchException{
		session = null;
		if(session == null) {
			JSch jsch = new JSch();
			session = jsch.getSession(user, ip, 22);
			System.out.println("session 1>>>>"+session);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(password);
			session.connect();
			logger.info("!!! session Connect>>>>"+session);

		}

		return session;
	}

	/**
	 * 	
	 * @param localFile
	 * @param fileName
	 * @return
	 */

	public static String uploadFileContent(Channel channel, String localFile, String fileName){
		String message="FAILED";
		ChannelSftp sftpChannel = null;

		try {
			sftpChannel = (ChannelSftp) channel;
			logger.debug("sftpChannel.getHome()::"+sftpChannel.getHome());
			logger.debug("sftpChannel.pwd()::"+sftpChannel.pwd());
			sftpChannel.put(localFile, remoteDir + fileName);
			message="SUCCESS";

		} catch (SftpException e) {
			e.printStackTrace();
			message="FAILED";
		}
		return message;
	} 
	
	
	/**
	 * 
	 * @param channel
	 * @param fileName
	 * @return
	 */

	public static Boolean fileStatsCheck(Channel channel, String fileName){
		boolean isFileExist=false;
		ChannelSftp sftpChannel = null;
		try {
			sftpChannel = (ChannelSftp) channel;
			logger.debug("sftpChannel.pwd()>::"+sftpChannel.pwd());
			attrs = sftpChannel.stat(sftpChannel.pwd()+fileName);
		} catch (Exception e) {
			attrs = null;
			logger.debug("File not found to the server "+fileName);
		}
		logger.debug("---attrs------"+attrs);
		if (attrs != null) {
			isFileExist=true;

		}
		return isFileExist;
}
	
	
	/**
	 * 
	 * @param removeFile
	 * @param channel
	 * @param localFile
	 * @param fileName
	 * @return
	 */
	public static String  UploadFileContent(Channel channel, String localFile, String fileName){
		logger.info("Method UploadFileContent START::>>");
		String message="FAILED";
		ChannelSftp sftpChannel = null;

		try {
			sftpChannel = (ChannelSftp) channel;
			logger.debug("sftpChannel.getHome()::"+sftpChannel.getHome());
			logger.debug("sftpChannel.pwd()::"+sftpChannel.pwd());
			sftpChannel.put(localFile, remoteDir + fileName);
			message="SUCCESS";

		} catch (SftpException e) {
			logger.error("****Exception in sftpChannel***::>>"+e.getMessage());
			e.printStackTrace();
			message="FAILED";
		}
		logger.info("Method UploadFileContent END::>>");
		return message;
	} 

	/**
	 * 
	 * @param channel
	 * @param fileName
	 * @return
	 */
	public static boolean removeFileContent(Channel channel, String fileName){
		
		boolean  isFileExist=false;
		ChannelSftp sftpChannel = null;

		try {
			sftpChannel = (ChannelSftp) channel;
			logger.debug("sftpChannel.getHome()::"+sftpChannel.getHome());
			logger.debug("sftpChannel.pwd()::>>>"+sftpChannel.pwd());
			
			sftpChannel.rm(INBOUND_DIR + fileName);
			isFileExist=true;

		} catch (SftpException e) {
			e.printStackTrace();
			isFileExist = false;
		}
		return isFileExist;
	} 

	/**
	 * Closing session 
	 */
	public static void closeSession(){
		try{
			if(session!=null){
				session.disconnect();
				session=null;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

	} 
	
	
	public static String DownloadFile(Channel channel, String downloadDir, String fileLocation, String downloadFile){
		logger.info("Method DownloadFile START::>>");
		String message="FAILED";
		ChannelSftp sftpChannel = null;

		try {
			sftpChannel = (ChannelSftp) channel;
			logger.debug("sftpChannel.getHome()::"+sftpChannel.getHome());
			logger.debug("sftpChannel.pwd()::"+sftpChannel.pwd());
			
			logger.debug("sftpChannel.pwd()>>>>>>>>>>>>>>>::"+sftpChannel.pwd());
			logger.debug("sftpChannel.pwd()::"+fileLocation+downloadFile);
			logger.debug("downloadFile.pwd()::"+downloadFile);
			logger.debug("fileLocation.pwd()::"+fileLocation);
			logger.debug("Inbound>>>>>>>>>>>>>>>>>::"+INBOUND_DIR+downloadFile);
			sftpChannel.get(downloadDir+downloadFile, fileLocation+downloadFile);
			message="SUCCESS";

		} catch (SftpException e) {
			logger.error("****Exception in DownloadFile sftpChannel***::>>"+e.getMessage());
			e.printStackTrace();
			message="FAILED";
		}
		logger.info("Method DownloadFile END::>>");
		return message;
	}
	
	
	public static ArrayList getListOfFile(Channel channel, String downloadDir){
		logger.info("Method getListOfFile START::>>");
		ChannelSftp sftpChannel = null;
		ArrayList<String> listOfFile = new ArrayList<String>();
           
		try {
			sftpChannel = (ChannelSftp) channel;
			logger.debug("sftpChannel.getHome()::"+sftpChannel.getHome());
			logger.debug("sftpChannel.pwd()::"+sftpChannel.pwd());
			
			//sftpChannel.cd(downloadDir);
			
			Vector<ChannelSftp.LsEntry> list = sftpChannel.ls(downloadDir+"*.csv");
            for(ChannelSftp.LsEntry entry : list) {
            	listOfFile.add(entry.getFilename());
                
            }
			
		} catch (SftpException e) {
			logger.error("****Exception in getListOfFile sftpChannel***::>>"+e.getMessage());
			e.printStackTrace();
		}
		logger.info("Method getListOfFile END::>>"+listOfFile);
		return listOfFile;
	}
	
	
}
