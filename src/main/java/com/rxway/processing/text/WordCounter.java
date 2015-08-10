package com.rxway.processing.text;

import com.sun.deploy.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import rx.Observable;
import rx.Subscriber;
import rx.functions.*;
import rx.observables.StringObservable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by bobby on 10/08/15.
 */
public class WordCounter {

	public static void main(String[] args){
		new WordCounter().processFile("./src/main/resources/cv732_13092.txt");
	}

	public void processFile(String path){
		final String[] ovels = new String[]{"a","e","i","o","u"};
		String str = parseFile(path);

		if(str != null)
		StringObservable
				.byLine(Observable.just(str))
				.flatMap(new Func1<StringObservable.Line, Observable<String[]>>() {
					public Observable<String[]> call(StringObservable.Line line) {
						return Observable.just(line.getText().split(" "));
					}
				}).collect(new Func0<HashMap<String, Object>>() {
			public HashMap<String, Object> call() {
				HashMap<String, Object> facets = new HashMap<String, Object>();
				HashMap<String, Object> stats = new HashMap<String, Object>();

				HashMap<String, Object> processed = new HashMap<String, Object>();
				processed.put("facets", facets);
				processed.put("stats", stats);

				return processed;
			}
		}, new Action2<HashMap<String, Object>, String[]>() {
			public void call(HashMap<String, Object> o, String[] words) {

				HashMap<String, Object> facets = (HashMap<String, Object>) o.get("facets");
				HashMap<String, Object> stats = (HashMap<String, Object>) o.get("stats");

				for (String word : words) {

					if (!facets.containsKey(word)) {
						facets.put(word, 1);
					} else {
						int c = Integer.parseInt(facets.get(word).toString());
						facets.put(word, ++c);
					}

					if (!stats.containsKey("smallestWord")) {
						stats.put("smallestWord", word);
						stats.put("biggestWord", word);
					} else {

						String smallest = stats.get("smallestWord").toString();
						String biggest = stats.get("biggestWord").toString();

						stats.put("smallestWord", smallest.length() < word.length() ? smallest : word);
						stats.put("biggestWord", biggest.length() > word.length() ? biggest : word);

					}

					if (!stats.containsKey("ovels")) {
						stats.put("ovels", new HashMap<String, Integer>());
					}
					try {
						for (String ovel : ovels) {
							HashMap<String, Integer> ovelsMap = (HashMap<String, Integer>) stats.get("ovels");
							if (word.length() >0 && ovel.equalsIgnoreCase(word.charAt(0) + "")) {
								if (ovelsMap.containsKey(ovel)) {
									Integer c = ovelsMap.get(ovel);
									ovelsMap.put(ovel, ++c);
								} else {
									ovelsMap.put(ovel, 1);
								}
							}
						}
					} catch (Exception e) {
						//WARNING: errors in deep callbacks silently failing..
						System.out.println(e.getMessage());
					}

				}
			}
		}).subscribe(new Subscriber<HashMap<String, Object>>() {
			public void onCompleted() {

			}

			public void onError(Throwable throwable) {

			}

			public void onNext(HashMap<String, Object> stats) {
				ObjectMapper mapper = new ObjectMapper();
				String json = "";
				try {
					json = mapper.defaultPrettyPrintingWriter().writeValueAsString(stats);
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println(json);
			}
		});


	}

	private String parseFile(String path){

		String str = null;

		try {
			str = FileUtils.readFileToString(new File(path)).trim();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return str;
	}

}
