package com.aotain.smmsapi.task.serviceapi;

import feign.Headers;
import feign.RequestLine;

@Headers({ "Content-Type: application/json", "Accept: application/json" })
public interface IDemoService {
	@RequestLine("GET /serviceapi/apidemo/sayhello")
    public UrlBean sayHello(UserBean ubean);
	
	@RequestLine("GET /serviceapi/apidemo/sayhello1")
    public UrlBean sayHello1(UserBean ubean);
	
	static class UserBean {
		private String name;
		
		public UserBean(String name){
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
	}
	static class UrlBean{
		private String url;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		@Override
		public String toString() {
			return "UrlBean [url=" + url + "]";
		}
		
	}
}


