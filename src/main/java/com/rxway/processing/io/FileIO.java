package com.rxway.processing.io;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import rx.Observable;
import rx.Subscriber;
import rx.functions.*;
import rx.util.async.Async;

import java.io.*;
import java.util.HashMap;

/**
 * Created by bobby on 12/08/15.
 */
public class FileIO {

	public static void main(String[] args){

		try {
			new FileIO().loadScrips();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private JsonArray jsonFile2JsonObj(String file) {

		JsonArray jo = null;

		try {

			StringBuilder jsonStr = null;
			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

			String strLine;
			jsonStr = new StringBuilder();

			while ((strLine = bufferedReader.readLine()) != null)   {
				jsonStr.append(strLine);
			}

			JsonParser jp = new JsonParser();
			jo  = (JsonArray) jp.parse(jsonStr.toString());

			//System.out.println(jo.size());

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return jo;
	}

	public void loadScrips() throws InterruptedException{

		JsonArray ja 	= jsonFile2JsonObj("./src/main/resources/BSE_Scrip_List.json");


		new File("./src/main/resources/async").mkdir();
		new File("./src/main/resources/plain").mkdir();

		//if(true)
		//return;

		Observable<JsonElement> observable = Observable.from(ja);

		final long start = System.currentTimeMillis();
		observable.doOnNext(new Action1<JsonElement>() {
			public void call(JsonElement element) {

				try {
					JsonObject o = (element).getAsJsonObject();
					boolean active = o.get("status").getAsString().trim().equalsIgnoreCase("active");
					boolean yahooReady = o.get("yahooReady").getAsString().trim().equalsIgnoreCase("true");
					String scid = o.getAsJsonObject().get("scid").getAsString();

					if (active && yahooReady) {
						//System.out.println(Thread.currentThread().getId());
							if (scid.charAt(0) == 'z' || scid.charAt(0) == 'Z') {
								//throw (new RuntimeException("System doesnt like z"));
							} else {
								FileWriter writer = new FileWriter(new File("./src/main/resources/plain/" + scid + ".json"));
								writer.write(o.toString());
								writer.flush();
								writer.close();
							}

						}

					}catch(IOException e){
						e.printStackTrace();
					}

				}
			}).subscribe(new Subscriber<JsonElement>() {

			public void onCompleted() {
				System.out.println("Plain Took " + (System.currentTimeMillis() - start) + "ms");
			}


			public void onError(Throwable e) {

			}


			public void onNext(JsonElement element) {

			}
		});

		final long asyncStart = System.currentTimeMillis();
		observable.flatMap(new Func1<JsonElement, Observable<Boolean>>() {

			public Observable<Boolean> call(JsonElement element) {

				Observable<Boolean> written = null;
				written = Async.asyncFunc(new FuncN<Boolean>() {

					public Boolean call(Object... args) {
						Boolean written = false;
						JsonObject o = ((JsonElement) args[0]).getAsJsonObject();

						boolean active = o.get("status").getAsString().trim().equalsIgnoreCase("active");
						boolean yahooReady = o.get("yahooReady").getAsString().trim().equalsIgnoreCase("true");

						try {

							if (active && yahooReady) {
								String scid = o.getAsJsonObject().get("scid").getAsString();
								if (scid.charAt(0) == 'z' || scid.charAt(0) == 'Z') {
									throw (new RuntimeException("System doesnt like z"));
								} else {
									//System.out.println("Writing with thrread " + (Thread.currentThread().getId()));
									FileWriter writer = new FileWriter(new File("./src/main/resources/async/" + scid + ".json"));
									writer.write(o.toString());
									writer.flush();
									writer.close();
									written = true;
								}

							} else {
								written = false;
							}

						} catch (IOException e) {
							e.printStackTrace();
							written = null;
						} catch (RuntimeException e) {
							//e.printStackTrace();
							written = null;
						} catch (Exception e) {
							//e.printStackTrace();
							written = null;
						}

						return written;

					}
				}).call(element);

				return written;

			}
		}).collect(new Func0<HashMap<String, Integer>>() {

			public HashMap<String, Integer> call() {
				return new HashMap<String, Integer>();
			}
		}, new Action2<HashMap<String, Integer>, Boolean>() {

			public void call(HashMap<String, Integer> o, Boolean written) {
				//System.out.println("written ? " + written);
				if (written == null) {

					if (o.containsKey("exception")) {
						int ec = o.get("exception");
						o.put("exception", ++ec);
					} else {
						o.put("exception", 1);
					}

				} else if (written) {

					if (o.containsKey("written")) {
						int bc = o.get("written");
						o.put("written", ++bc);
					} else {
						o.put("written", 1);
					}

				} else {

					if (o.containsKey("failed")) {
						int sc = o.get("failed");
						o.put("failed", ++sc);
					} else {
						o.put("failed", 1);
					}

				}
			}
		}).subscribe(new Subscriber<HashMap<String, Integer>>() {

			public void onStart() {

			}

			public void onCompleted() {
				System.out.println("Async Took " + (System.currentTimeMillis() - asyncStart) + "ms");

			}

			public void onError(Throwable e) {
				System.out.println(e.getMessage());
			}

			public void onNext(HashMap<String, Integer> element) {
				System.out.println(element);
			}
		});


		//Stop the main thread from exiting, (Observable.toBlocking().single() also can be used)
		try {
			System.out.println("\n---------------\nPRESS ENTER IN CONSOLE TO EXIT THE PROCESS.\n---------------\n");
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {

			System.out.println("REMOVING THE CREATED TEMP FILES");
			FileUtils.deleteDirectory(new File("./src/main/resources/async"));
			FileUtils.deleteDirectory(new File("./src/main/resources/plain"));

		} catch (IOException e) {
			e.printStackTrace();
		}


	}
}
