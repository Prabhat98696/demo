package com.agron.wc.integration.straub.Inbound;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.agron.wc.integration.straub.util.AgronSFTPServerConfig;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.lcs.wc.client.ClientContext;
import com.lcs.wc.util.FormatHelper;
import com.lcs.wc.util.LCSProperties;

import wt.org.WTUser;
import wt.util.WTException;
import wt.util.WTProperties;

public class AgronStraubInboundUploader {
	
	public static String tempDownloadFilePath = null;
	public static String archiveInboundfile = null;
	public static String wtHomePath = null;
	public static WTProperties wtProperties;

	private static final Logger log = LogManager.getLogger("com.agron.wc.integration.straub.inbound");
	public static String INBOUND_DIR = LCSProperties.get("com.agron.wc.integration.straub.inbound.upload.fileDirPath");
	
	static{
		try{

			wtProperties = WTProperties.getLocalProperties();
			wtHomePath = wtProperties.getProperty("wt.home");
			tempDownloadFilePath = wtHomePath + FormatHelper.formatOSFolderLocation(LCSProperties.get("com.agron.wc.integration.straub.Inbound.tempDownloadFilePath"));
			archiveInboundfile = wtHomePath + FormatHelper.formatOSFolderLocation(LCSProperties.get("com.agron.wc.integration.straub.Inbound.archiveStraubFile"));
			new File(tempDownloadFilePath).mkdirs();
			new File(archiveInboundfile).mkdirs();
		}catch(IOException io){
			io.printStackTrace();
		}

	}
	
	
	public static void executeStart(){
		log.debug(":::ExecuteStart::>>");
		ArrayList<String> archiveFileList = new ArrayList<String>();
		try {
			ArrayList<String> listOfFileOnSFTP = downloadFileFromFTP();
			log.debug("::::ListOfUploadFromFileOnSFTP::>>"+listOfFileOnSFTP);
			if(listOfFileOnSFTP.size()>0){
				for(int i=0; i<listOfFileOnSFTP.size(); i++){
					readFileAndUploadData(listOfFileOnSFTP.get(i));
					archiveFileList.add(listOfFileOnSFTP.get(i));
				}
				AgronSFTPServerConfig.closeSession();
				
				// Archive File From sftp and store in to archive File folder on local
				archiveFileFromsftp(archiveFileList);
				
				// Delete Temp Folder file 
				//deleteTempDownloadFileFolder();
			}
		} catch (JSchException | WTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void deleteTempDownloadFileFolder(){
		log.debug("::::DELETE deleteTempDownloadFileFolder:: START::");
		File tempFileFolder = new File(tempDownloadFilePath);
		String[]entries = tempFileFolder.list();
		for(String s: entries){
		    File currentFile = new File(tempFileFolder.getPath(),s);
		    currentFile.delete();
		}
		tempFileFolder.delete();
		log.debug("**********DELETE deleteTempDownloadFileFolder:: END::*******************");
	}
	
	/**
	 * This method is use for delete file after upload
	 * @param filename
	 * @return
	 */
	public static void archiveFileFromsftp(ArrayList archiveFileList){
		try {
			log.debug("::::ArchiveFileFromsftp START::>>"+archiveFileList);
			Channel channel= getsftpChannel();
			if(archiveFileList.size()>0){
				for(int i=0; i<archiveFileList.size(); i++){
					String message = AgronSFTPServerConfig.DownloadFile(channel, INBOUND_DIR, archiveInboundfile, archiveFileList.get(i).toString());
					if("SUCCESS".equals(message)){
						AgronSFTPServerConfig.removeFileContent(channel, archiveFileList.get(i).toString().trim());
					}
				}
			}
			AgronSFTPServerConfig.closeSession();
			log.debug("::::archiveFileFromsftp END::>>");
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	
	/**
	 * This method is use for getting Channel
	 * @return
	 * @throws JSchException
	 */
	public static Channel getsftpChannel() throws JSchException{
		Channel channel = null;
		if(channel == null){
			Session session = AgronSFTPServerConfig.getSFTPSession();
			if(session!= null){
				channel = session.openChannel("sftp");
				if(channel!= null){
					channel.connect();
				}
			}
		}
		return channel; 
	}
	
	/**
	 * This method is use for download file in temp folder from sftp 
	 * @return
	 * @throws JSchException
	 */
	public static ArrayList downloadFileFromFTP() throws JSchException{
		ArrayList<String> listOfFile = new ArrayList<String>();
		
		Channel channel = null;
		if(channel == null){
			Session session = AgronSFTPServerConfig.getSFTPSession();
			log.debug("::::downloadFileFromFTP START Session ::>>"+session);
			if(session!= null){
				channel = session.openChannel("sftp");
				if(channel!= null){
					channel.connect();
				}
			}
		}
		
		if(channel != null){
			listOfFile = AgronSFTPServerConfig.getListOfFile(channel, INBOUND_DIR);
			log.debug(":::: listOfFile Size ::>>"+listOfFile.size());
			if(listOfFile.size()>0){
				for(int i=0; i<listOfFile.size(); i++){
					log.debug(":::: File Name ::>>"+listOfFile.get(i));
					String fileDownload = AgronSFTPServerConfig.DownloadFile(channel, INBOUND_DIR, tempDownloadFilePath, listOfFile.get(i));
				}
			}
		}
		
		return listOfFile;
	}



	/**
	 * This method is issue for read file from temp folder and upload to PLM
	 * @param fileName
	 * @return
	 * @throws WTException
	 */
	public static String readFileAndUploadData(String fileName) throws WTException{
		log.debug(":::: ReadFileAndUploadData START ::>>");
		 	ClientContext lcsContext = ClientContext.getContext();
			WTUser user = lcsContext.getUser();
			log.debug("Straub loadData() - Loading Data in Method Server.");
			AgronStraubDataLoader loader = new AgronStraubDataLoader();
			String message = loader.load(user, tempDownloadFilePath+fileName, true);
			log.debug(":::: ReadFileAndUploadData END ::>>");
			return message;
		 
	}

}
