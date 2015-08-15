package com.rxway.processing.image;


import com.google.gson.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.face.detection.CLMDetectedFace;
import org.openimaj.image.processing.face.detection.CLMFaceDetector;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.FuncN;
import rx.util.async.Async;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaceDetection {

	public static void main(String[] args){
		new FaceDetection().processImages("sunand padmanabhan");

		/*try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}

	private File loadImage(String url, String name){

		URL _url = null;
		File file = new File("./src/main/resources/images/" + name);

		try {

			System.out.println("Loading images on thread " + Thread.currentThread().getId());
			_url = new URL(url);
			FileUtils.copyURLToFile(_url, file);

		}catch (Exception e) {
			//e.printStackTrace();
		}

		return file;

	}

	public void processImages(String keyWord){

		JsonArray result = loadImagesSearchResult(keyWord);

		List<Map<String, Object>> maps = null;
		maps = Observable.from(result).flatMap(new Func1<JsonElement, Observable<File>>() {

			public Observable<File> call(JsonElement element) {

				String url = ((JsonObject) element).get("tbUrl").getAsString();
				String name = ((JsonObject) element).get("unescapedUrl").getAsString();
				String[] chunks = name.split("/");
				name = chunks[chunks.length - 1];

				return Async.toAsync(new FuncN<File>() {
					public File call(Object... args) {
						return loadImage((String) args[0], (String) args[1]);
					}
				}).call(url, name);

			}
		}).flatMap(new Func1<File, Observable<Map<String, Object>>>() {

			public Observable<Map<String, Object>> call(File file) {

				if(file.exists()) {
					return Async.toAsync(new FuncN<Map<String, Object>>() {
						public Map<String, Object> call(Object... args) {
							return detectFaces((File) args[0]);
						}
					}).call(file);
				}else{
					return null;
				}

			}
		}).toList().toBlocking().single();
						/*.subscribe(new Action1<List<Map<String, Object>>>() {
					public void call(List<Map<String, Object>> maps) {
						System.out.println(new Gson().toJson(maps));
					}
				});*/

						System.out.println(new Gson().toJson(maps));


	}

	private Map<String, Object> detectFaces(File another){

		HashMap<String, Object> map = new HashMap<String, Object>();

		if(another == null){
			return map;
		}

		try {

			System.out.println("Detecting faces on thread " + Thread.currentThread().getId());

			map = new HashMap<String, Object>();
			FImage img = ImageUtilities.readF(another);
			MBFImage mbf = ImageUtilities.readMBF(another);
			File detected = new File("./src/main/resources/images/detected/"+another.getName());

			CLMFaceDetector det3 = new CLMFaceDetector();

			List<CLMDetectedFace> faces = det3.detectFaces(img);

			map.put("original", another.getPath());
			map.put("processed", detected.getPath());

			if (faces.size() > 0) {

				CLMDetectedFace face3 = faces.get(0);
				mbf.drawShape(face3.getBounds(), 1, RGBColour.WHITE);

				map.put("imgBounds", img.getBounds());
				map.put("faceBounds", face3.getBounds());

				ImageUtilities.write(mbf, detected);

			}


		} catch (IOException e) {
			e.printStackTrace();
		}

		return map;
	}



	private JsonArray loadImagesSearchResult(String keyWord){

		try {
			keyWord = URLEncoder.encode(keyWord, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			keyWord = "";
			e.printStackTrace();
		}

		JsonArray result 	= new JsonArray();
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod("https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q="+ keyWord);

		//method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));


		try {
			// Execute the method.
			int statusCode = client.executeMethod(method);

			if (statusCode != HttpStatus.SC_OK) {

				System.err.println("Method failed: " + method.getStatusLine());

			}else{

				byte[] responseBody = method.getResponseBody();

				result = new JsonParser()
						.parse(new String(responseBody))
						.getAsJsonObject().get("responseData")
						.getAsJsonObject().get("results")
						.getAsJsonArray();
			}

		} catch (HttpException e) {
			System.err.println("Fatal protocol violation: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("transport error: " + e.getMessage());
		} finally {
			method.releaseConnection();
		}

		return result;

	}


}
