package kr.co.uxn.agms.ftp;

import android.os.Environment;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;

public class FTPManager {

    private static final String TAG="FTPManger";
    private final FTPClient mFTPClient;

    private final String FTP_HOST="uxn-always.synology.me";
    private final String FTP_PASSWORD="uxn1234A##";
    private final int FTP_PORT=506;

    private static FTPManager instance;

    public static FTPManager getInstance() {
        if (instance == null) {

            instance=new FTPManager();
        }
        return instance;
    }

    public FTPManager() {
        mFTPClient=new FTPClient();
    }


    public boolean ftpConnect(String id) {
        boolean result=false;
        try {
            mFTPClient.setControlEncoding("euc-kr");
            mFTPClient.connect(FTP_HOST, FTP_PORT);

            if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {
                result=mFTPClient.login(id, FTP_PASSWORD);
                mFTPClient.enterLocalPassiveMode();
            }
        } catch (Exception e) {
            Log.d(TAG, "연결 실패2");
            Log.d(TAG,e.toString());
        }
        return result;
    }

    public boolean ftpDisconnect() {
        boolean result=false;
        try {
            mFTPClient.logout();
            mFTPClient.disconnect();
            result=true;
        } catch (Exception e) {
            Log.d(TAG, "서버와 클라 연결 해제 실패 ");
        }
        return result;
    }

    public boolean ftpUploadFile(String desfilePath) {
        boolean result=false;
        try {
            mFTPClient.cwd(desfilePath);
            mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);


            File path=new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "AGMS/");


            if (path.listFiles().length > 0) {
                for (File file : path.listFiles()) {
                    if (file.isFile()) {
                        FileInputStream ifile=new FileInputStream(file);
                        mFTPClient.deleteFile(file.getName());
                        result=mFTPClient.appendFile(file.getName(), ifile);
                    }
                }
            }

        } catch (Exception e) {
            Log.d(TAG, "Couldn't upload the file");
        }
        return result;
    }
}
