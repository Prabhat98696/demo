package com.agron.wc.sftpImageExport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	
	static String ip= LCSProperties.get("com.agron.wc.sftpImageExport.sftpRemote.host");
	static String user=LCSProperties.get("com.agron.wc.sftpImageExport.sftpRemote.user");
	static String password=LCSProperties.get("com.agron.wc.sftpImageExport.sftpRemote.password");
	static String remoteDir =LCSProperties.get("com.agron.wc.sftpImageExport.sftpRemote.dir");
	static String reNameDir =LCSProperties.get("com.agron.wc.sftpImageExport.sftpRemote.bkpDir");
	static Session session = null;
	static SftpATTRS attrs=null;
	
	static final Logger logger = LogManager.getLogger("com.agron.wc.sftpImageExport");


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

	/*public static String uploadFileContent(Channel channel, String localFile, String fileName){
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
	} */
	
	
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
			attrs = sftpChannel.stat(sftpChannel.pwd()+"/"+remoteDir+fileName);
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
	public static String removeAndUploadFileContent(String removeFile, Channel channel, String localFile, String fileName){
		String message="FAILED";
		ChannelSftp sftpChannel = null;

		try {
			sftpChannel = (ChannelSftp) channel;
			logger.debug("sftpChannel.getHome()::"+sftpChannel.getHome());
			logger.debug("sftpChannel.pwd()::"+sftpChannel.pwd());
			logger.debug("fileStatsCheck before::"+fileStatsCheck(channel, removeFile));
			if(fileStatsCheck(channel, removeFile) && removeFile!= null){
				if(removeFile.contains("_30_CAD")){
					sftpChannel.rm(remoteDir + removeFile);
				}else{
					sftpChannel.put(localFile, remoteDir + removeFile);
				}
			} 
			sftpChannel.put(localFile, remoteDir + fileName);
			message="SUCCESS";
			
		} catch (SftpException e) {
			e.printStackTrace();
			message="FAILED";
		}
		logger.debug("Message ******************::"+message);
		return message;
	} 


	/**
	 * This method is use for upload content
	 * @param channel
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
			if(FormatHelper.hasContent(fileName)){
			sftpChannel.put(localFile, remoteDir + fileName);
			}
			message="SUCCESS";
			
		} catch (SftpException e) {
			e.printStackTrace();
			message="FAILED";
		}
		logger.debug("Upload file *uploadFileContent***::"+message);
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
			logger.debug("sftpChannel.pwd()::"+sftpChannel.pwd());
			boolean filecheck = fileStatsCheck(channel, fileName);
			if(filecheck){
			sftpChannel.rm(remoteDir + fileName);
			}
			isFileExist=filecheck;

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
	
	/**
	 * 
	 * @param channel
	 * @param sftpDir
	 * @return directreated
	 */
	public static Boolean renameORCreateDirectory(Channel channel){
		boolean isFileExist=false;
		ChannelSftp sftpChannel = null;
		try {
			sftpChannel = (ChannelSftp) channel;
			sftpChannel.rename(remoteDir, reNameDir);
			sftpChannel.mkdir(remoteDir);
			isFileExist=true;
		} catch (Exception e) {
			logger.info("******Dir not to the server "+remoteDir);
		}
		
		try{
			logger.info("---attrs---renameORCreateDirectory---"+attrs);
			if (attrs!= null) {
				logger.info("Creating dir "+remoteDir);
				sftpChannel.mkdir(remoteDir);
				isFileExist=true;
			}
		} catch (Exception e) {
			isFileExist=false;
			e.printStackTrace();
		}
		return isFileExist;
	}
}
