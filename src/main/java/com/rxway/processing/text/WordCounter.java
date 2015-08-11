package com.rxway.processing.text;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.functions.Func1;
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
		final String[] vowels = new String[]{"a","e","i","o","u"};
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
			public void call(HashMap<String, Object> o, String[] tokens) {

				HashMap<String, Object> facets = (HashMap<String, Object>) o.get("facets");
				HashMap<String, Object> stats = (HashMap<String, Object>) o.get("stats");

				for (String token : tokens) {

					if (!facets.containsKey(token)) {
						facets.put(token, 1);
					} else {
						int c = Integer.parseInt(facets.get(token).toString());
						facets.put(token, ++c);
					}

					if (!stats.containsKey("smallestWord")) {
						stats.put("smallestWord", token);
						stats.put("biggestWord", token);
					} else {

						String smallest = stats.get("smallestWord").toString();
						String biggest = stats.get("biggestWord").toString();

						stats.put("smallestWord", smallest.length() < token.length() ? smallest : token);
						stats.put("biggestWord", biggest.length() > token.length() ? biggest : token);

					}

					if (!stats.containsKey("vowels")) {
						stats.put("vowels", new HashMap<String, Integer>());
					}
					try {
						for (String vowel : vowels) {
							HashMap<String, Integer> vowelsMap = (HashMap<String, Integer>) stats.get("vowels");
							if (token.length() >0 && vowel.equalsIgnoreCase(token.charAt(0) + "")) {
								if (vowelsMap.containsKey(vowel)) {
									Integer c = vowelsMap.get(vowel);
									vowelsMap.put(vowel, ++c);
								} else {
									vowelsMap.put(vowel, 1);
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
