package com.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MyClientActivity extends Activity 
{
    private EditText mEditText = null;
    private Button connectButton = null;
    private Button sendButton = null;
    private TextView mTextView = null;
    
    private Socket clientSocket = null;
    private OutputStream outStream = null;
    
    private Handler mHandler = null;
    
    private ReceiveThread mReceiveThread = null;
    private boolean stop = true;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mEditText = (EditText)this.findViewById(R.id.edittext);
        mTextView = (TextView)this.findViewById(R.id.retextview);
        connectButton = (Button)this.findViewById(R.id.connectbutton);
        sendButton = (Button)this.findViewById(R.id.sendbutton);
        sendButton.setEnabled(false);      
        
        //���Ӱ�ť����
        connectButton.setOnClickListener(new View.OnClickListener() 
        {
            
            @Override
            public void onClick(View v) 
            {
                // TODO Auto-generated method stub
                try 
                {
                    //ʵ�����������ӵ�������
                    clientSocket = new Socket("10.0.2.2",6100);
                } 
                catch (UnknownHostException e) 
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } 
                catch (IOException e) 
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                displayToast("���ӳɹ���");                        
                //���Ӱ�ťʹ��
                connectButton.setEnabled(false);
                //���Ͱ�ťʹ��
                sendButton.setEnabled(true);
                
                mReceiveThread = new ReceiveThread(clientSocket);
                stop = false;
                //�����߳�
                mReceiveThread.start();
            }
        });
        
        //�������ݰ�ť����
        sendButton.setOnClickListener(new View.OnClickListener() 
        {
            
            @Override
            public void onClick(View v) 
            {
                // TODO Auto-generated method stub
                byte[] msgBuffer = null;
                //���EditTex������
                String text = mEditText.getText().toString();
                try {
                    //�ַ�����ת��
                    msgBuffer = text.getBytes("GB2312");
                } catch (UnsupportedEncodingException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                            
                
                try {
                    //���Socket�������
                    outStream = clientSocket.getOutputStream();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }                                                    
                
                
                try {
                    //��������
                    outStream.write(msgBuffer);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //�������
                mEditText.setText("");
                displayToast("���ͳɹ���");
            }
        });
              
        //��Ϣ����
        mHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                //��ʾ���յ�������
                mTextView.setText((msg.obj).toString());
            }
        };
        
    }
    
    //��ʾToast����
    private void displayToast(String s)
    {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
    
    
    private class ReceiveThread extends Thread
    {
        private InputStream inStream = null;
        
        private byte[] buf;  
        private String str = null;
        
        ReceiveThread(Socket s)
        {
            try {
                //���������
                this.inStream = s.getInputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }      
        
        @Override
        public void run()
        {
            while(!stop)
            {
                this.buf = new byte[512];
                
                try {
                    //��ȡ�������ݣ�������
                    this.inStream.read(this.buf);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } 
                
                //�ַ�����ת��
                try {
                    this.str = new String(this.buf, "GB2312").trim();
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                Message msg = new Message();
                msg.obj = this.str;
                //������Ϣ
                mHandler.sendMessage(msg);
                
            }
        }
        
        
    }
    
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        
        if(mReceiveThread != null)
        {
            stop = true;
            mReceiveThread.interrupt();
        }
    }
     
}