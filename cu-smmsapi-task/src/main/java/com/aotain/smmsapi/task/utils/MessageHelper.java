package com.aotain.smmsapi.task.utils;

import java.util.Arrays;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

public class MessageHelper {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private static volatile MessageHelper messageHelper;
	private static final Object mutex = new Object();

	@Autowired
	private MessageSource messageSource;

	public String getMessage(String code, Locale locale) {
		return getMessage(code, null, locale);
	}

	public String getMessage(String code, Object[] args, Locale locale) {
		try {
			Locale useLocale;
			if (StringUtil.isEmptyString(code)) {
				log.warn("Passed-in code is null or empty!");
				return "";
			}
			if (locale == null) {
				useLocale = Locale.getDefault();
			} else {
				useLocale = locale;
			}
			if (args == null) {
				args = new Object[] {};
			}
			return getMessageSource().getMessage(code, args, useLocale);
		} catch (NoSuchMessageException ex) {
			log.warn("Unable to find message from resource bundle," + code + "," + Arrays.asList(args));
			return code;
		}
	}

	/**
	 * Create instance of Spring Container should be called by Spring framework
	 * only.
	 * 
	 * @return New instance of SpringContainer
	 */
	public static MessageHelper createInstance() {
		if (messageHelper == null) {
			synchronized (mutex) {
				messageHelper = new MessageHelper();
			}
		}
		return messageHelper;
	}

	/**
	 * Construct for MessageHelper
	 */
	private MessageHelper() {
		super();
	}

	/**
	 * @return Singleton instance of message helper
	 */
	public static MessageHelper getHelper() {
		return messageHelper;
	}

	/**
	 * @param messageSource
	 *            set the value of messageSource
	 */
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	/**
	 * @return the value for messageSource
	 */
	private MessageSource getMessageSource() {
		return messageSource;
	}
	
}
