package com.nvapp.mqtt.service;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.content.Context;

/**
 * 文件存储方式
 *
 */
public class FileClientInfoStore implements IClientInfoStore {
	private Context context;

	public FileClientInfoStore(Context context) {
		this.context = context;
	}

	@Override
	public void store(ClientInfo clientInfo) {
		String content = clientInfo.getTenantId() + "," + clientInfo.getMobile() + "," + clientInfo.getRandomId();

		writeFiles("clientinfo.cfg", content);
	}

	@Override
	public ClientInfo read() {
		String content = readFiles("clientinfo.cfg");

		String[] vs = content.split(",");
		if (vs.length == 3) {
			String tenantId = vs[0];
			String mobile = vs[1];
			String randomId = vs[2];

			if (tenantId != null && mobile != null) {
				return new ClientInfo(tenantId, mobile, randomId);
			}
		}

		return null;
	}

	/**
	 * 保存文件内容
	 * 
	 * @param fileName 文件名称
	 * @param content 内容
	 */
	private void writeFiles(String fileName, String content) {
		try {
			// 打开文件获取输出流，文件不存在则自动创建
			FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			fos.write(content.getBytes());
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 读取文件内容
	 * 
	 * @param fileName 文件名称
	 * @return 返回文件内容
	 */
	private String readFiles(String fileName) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			FileInputStream fis = context.openFileInput(fileName);
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = fis.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}
			String content = baos.toString();
			fis.close();
			baos.close();

			return content;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

}
