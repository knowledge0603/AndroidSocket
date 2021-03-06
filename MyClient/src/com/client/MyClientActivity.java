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
        
        //连接按钮监听
        connectButton.setOnClickListener(new View.OnClickListener() 
        {
            
            @Override
            public void onClick(View v) 
            {
                // TODO Auto-generated method stub
                try 
                {
                    //实例化对象并连接到服务器
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
                
                displayToast("连接成功！");                        
                //连接按钮使能
                connectButton.setEnabled(false);
                //发送按钮使能
                sendButton.setEnabled(true);
                
                mReceiveThread = new ReceiveThread(clientSocket);
                stop = false;
                //开启线程
                mReceiveThread.start();
            }
        });
        
        //发送数据按钮监听
        sendButton.setOnClickListener(new View.OnClickListener() 
        {
            
            @Override
            public void onClick(View v) 
            {
                // TODO Auto-generated method stub
                byte[] msgBuffer = null;
                //获得EditTex的内容
                String text = mEditText.getText().toString();
                try {
                    //字符编码转换
                    msgBuffer = text.getBytes("GB2312");
                } catch (UnsupportedEncodingException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                            
                
                try {
                    //获得Socket的输出流
                    outStream = clientSocket.getOutputStream();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }                                                    
                
                
                try {
                    //发送数据
                    outStream.write(msgBuffer);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //清空内容
                mEditText.setText("");
                displayToast("发送成功！");
            }
        });
              
        //消息处理
        mHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg)
            {
                //显示接收到的内容
                mTextView.setText((msg.obj).toString());
            }
        };
        
    }
    
    //显示Toast函数
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
                //获得输入流
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
                    //读取输入数据（阻塞）
                    this.inStream.read(this.buf);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } 
                
                //字符编码转换
                try {
                    this.str = new String(this.buf, "GB2312").trim();
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                Message msg = new Message();
                msg.obj = this.str;
                //发送消息
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