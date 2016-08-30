package com.control.amigo;

import java.io.File;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amigo.R;

public class ModifyIPDialog extends DialogFragment {
	private TextView currentIptxt;
	private EditText newIptxt;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		LayoutInflater mInflater = LayoutInflater.from(getActivity());
		View dialogView = mInflater.inflate(R.layout.modifyipdialog_view, null);
		currentIptxt = (TextView)dialogView.findViewById(R.id.currentip_text);
		newIptxt = (EditText)dialogView.findViewById(R.id.newip_edittext);
		currentIptxt.setText("目前IP : "+MonitorService.getserverIP());
		
		AlertDialog.Builder dialogBuilder = new Builder(getActivity());
		dialogBuilder.setTitle("Do you modify the server IP?")
		.setView(dialogView)
		.setPositiveButton("Save", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				String newip = newIptxt.getText().toString();
				if( isIP(newip) ){
					MonitorService.setserverIP(newip);
					FragtabsActivity.ServerIP = newip;
					
					String filename = "ServerIP.txt";
					File inFile = new File(getActivity().getFilesDir(), filename);
					try {
						FileWriter fileOut = new FileWriter(inFile);
						fileOut.write(newip);
						fileOut.flush();
						fileOut.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					Toast.makeText(getActivity(), "IP修改成功", Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(getActivity(), "IP修改失敗", Toast.LENGTH_SHORT).show();
				}
			}
		})
		.setNegativeButton("Cancel", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		
		return dialogBuilder.create();
	}
	
	public boolean isIP(String addr){  
        if(addr.length() < 7 || addr.length() > 15 || "".equals(addr)){  
            return false;
        }  
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pat = Pattern.compile(rexp);  
        Matcher mat = pat.matcher(addr);    
        boolean ipAddress = mat.find();  
        return ipAddress;  
    }  
	
}
