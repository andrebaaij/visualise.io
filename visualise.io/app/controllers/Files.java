package controllers;

import java.util.ArrayList;

import play.mvc.Controller;
import play.mvc.Result;
import play.data.DynamicForm;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;

//import sun.misc.BASE64Encoder;
//import sun.misc.BASE64Decoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import com.google.common.io.*;
import com.google.common.base.*;

public class Files extends Controller {
  
	public static Result download() {
		System.out.println("begin request");
		DynamicForm requestData = DynamicForm.form().bindFromRequest();

		String encoding = "url";
		String data  = requestData.get("data");
		String filename = requestData.get("filename");
		String[] tmp = data.split(",");
		String[] parts = tmp[0].split(";");
		String type = null;
		String charset = null;
		String body = null;
		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];
			if (part.toLowerCase().startsWith("data:")) {
				type = part.substring(5);
			} else if (part.toLowerCase().startsWith("charset=")) {
				charset = part.substring(8);
			} else if (part.toLowerCase().startsWith("base")) {
				encoding = part;
			}
		}

		if (filename == null) {
			filename = "download." + type.split("/")[1];
		}
		
		
		response().setContentType("application/force-download");  
		response().setHeader("Content-disposition","attachment; filename=" + filename);
		if (encoding.equalsIgnoreCase("base64")) {
	        return ok(Base64.decodeBase64(tmp[1]));
		} else {
			return ok(tmp[1]);
		}
	}
}










