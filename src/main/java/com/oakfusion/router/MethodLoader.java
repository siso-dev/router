package com.oakfusion.router;



import java.lang.reflect.Method;

public class MethodLoader {

	public Method load(Class controller, String controllerMethod) throws NoSuchMethodException {

		Method methodFromQueryingClass = null;

		for (Method method : controller.getMethods()) {
			if (method.getName().equals(controllerMethod)) {
				if (methodFromQueryingClass == null) {
					methodFromQueryingClass = method;
				} else {
					throw new NoSuchMethodException(controllerMethod);
				}
			}
		}

		if (methodFromQueryingClass == null) {
			throw new NoSuchMethodException(controllerMethod);
		}
		return methodFromQueryingClass;
	}

}
