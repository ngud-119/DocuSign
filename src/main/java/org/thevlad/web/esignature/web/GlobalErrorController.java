package org.thevlad.web.esignature.web;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.NotReadablePropertyException;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
@Controller
public class GlobalErrorController implements ErrorController {

	public static final String DEFAULT_ERROR_VIEW = "/error/err";
	public static final String NOTFOUND_ERROR_VIEW = "/error/notfound";
    private final static String ERROR_PATH = "/error";

	private ErrorAttributes errorAttributes;

	public GlobalErrorController(ErrorAttributes errorAttributes) {
		this.errorAttributes = errorAttributes;
	}

	@RequestMapping(value = ERROR_PATH, produces = "text/html")
	public ModelAndView errorHtml(WebRequest request) {
		Map<String,Object> errAttrs = getErrorAttributes(request, false);
		for (Iterator<Entry<String, Object>> iterator = errAttrs.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, Object> entry = iterator.next();
			System.out.printf("%s : %s\n", entry.getKey(), entry.getValue().toString());
			
		}
		return new ModelAndView("/error/err",errAttrs );
	}
//	@GetMapping("/notfound")
//	public ModelAndView errorHandler(HttpServletRequest req, Exception ex) {
//		// Get status code to determine which view should be returned
//		Object statusCodeObj = req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
//		Enumeration<String> headaerNames = req.getHeaderNames();
//		while(headaerNames.hasMoreElements()) {
//			String headerName = headaerNames.nextElement();
//			String header = req.getHeader(headerName);
//			System.out.printf("%s : %s\n",headerName, header);
//		}
//		// In this case, status code will be shown in a view
//		ModelAndView mav = null;
//		if (statusCodeObj != null && statusCodeObj instanceof Integer) {
//			Integer statusCode = (Integer) statusCodeObj;
//			switch (statusCode.intValue()) {
//			case 404: {
//				mav = new ModelAndView(NOTFOUND_ERROR_VIEW);
//				mav.addObject("url", req.getAttribute("javax.servlet.forward.request_uri"));
//				break;
//			}
//			default: {
//				mav = new ModelAndView(DEFAULT_ERROR_VIEW);
//				mav.addObject("err", ex);
//				break;
//			}
//			}
//		}
//		return mav;
//	}

	@ExceptionHandler(Throwable.class)
	public ModelAndView defaultErrorHandler(HttpServletRequest req, Throwable e) throws Throwable {
		// If the exception is annotated with @ResponseStatus rethrow it and let
		// the framework handle it - like the OrderNotFoundException example
		// at the start of this post.
		// AnnotationUtils is a Spring Framework utility class.
		if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null) {
			throw e;
		}

		// Otherwise setup and send the user to a default error-view.
		ModelAndView mav = new ModelAndView();
		mav.addObject("err", e);
		mav.addObject("url", req.getRequestURL());
		mav.setViewName(DEFAULT_ERROR_VIEW);
		return mav;
	}

	@ExceptionHandler(NotReadablePropertyException.class)
	public ModelAndView runtimeErrorHandler(HttpServletRequest req, Throwable e) throws Throwable {
		// If the exception is annotated with @ResponseStatus rethrow it and let
		// the framework handle it - like the OrderNotFoundException example
		// at the start of this post.
		// AnnotationUtils is a Spring Framework utility class.
		if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null) {
			throw e;
		}

		// Otherwise setup and send the user to a default error-view.
		ModelAndView mav = new ModelAndView();
		mav.addObject("err", e);
		mav.addObject("url", req.getRequestURL());
		mav.setViewName(DEFAULT_ERROR_VIEW);
		return mav;
	}

	@Override
	public String getErrorPath() {
		// TODO Auto-generated method stub
		return "/error";
	}

//	@ExceptionHandler(Exception.class)
//	public ModelAndView handleAnyException(Exception ex, HttpServletRequest request, HttpServletResponse response) {
//		ModelAndView model = new ModelAndView();
//
//		model.addObject("err", ex);
//		model.setViewName("err");
//		if (ex == null || ex.getMessage() == null) {
//			if (response.getStatus() == HttpServletResponse.SC_NOT_FOUND) {
//				String errMsg = "Resource not found at path: " + request.getPathInfo();
//				model.addObject("errMsg", errMsg);
//			}
//		}
//		return model;
//	}

	
	 private boolean getTraceParameter(HttpServletRequest request) {
	        String parameter = request.getParameter("trace");
	        if (parameter == null) {
	            return false;
	        }
	        return !"false".equals(parameter.toLowerCase());
	    }

	    private Map<String, Object> getErrorAttributes(WebRequest request,
	                                                   boolean includeStackTrace) {
//	        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
	        return this.errorAttributes.getErrorAttributes(request, includeStackTrace);
	    }

	    private HttpStatus getStatus(HttpServletRequest request) {
	        Integer statusCode = (Integer) request
	                .getAttribute("javax.servlet.error.status_code");
	        if (statusCode != null) {
	            try {
	                return HttpStatus.valueOf(statusCode);
	            }
	            catch (Exception ex) {
	            }
	        }
	        return HttpStatus.INTERNAL_SERVER_ERROR;
	    }
}
