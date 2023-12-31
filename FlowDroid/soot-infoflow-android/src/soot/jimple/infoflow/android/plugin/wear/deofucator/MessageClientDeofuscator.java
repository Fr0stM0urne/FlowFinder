package soot.jimple.infoflow.android.plugin.wear.deofucator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.jimple.infoflow.plugin.wear.extras.ExtraTypes;

public class MessageClientDeofuscator {

	private static MessageClientDeofuscator instance = null;
	SootClass mclientClass;
	HashMap<String, String> obfuscationMap;
	HashMap<String, String> obfuscationMapCallback;
	DeofuscatorUtil deobUtil;

	public HashMap<String, String> getObfuscationMap() {
		return obfuscationMap;
	}

	public HashMap<String, String> getObfCallbackMap() {
		return obfuscationMapCallback;
	}

	public static MessageClientDeofuscator getInstance() {
		if (instance == null)
			instance = new MessageClientDeofuscator();
		return instance;
	}

	private MessageClientDeofuscator() {
		this.mclientClass = Scene.v().getSootClass(ExtraTypes.MESSAGE_CLIENT);
		obfuscationMap = new HashMap<String, String>();
		obfuscationMapCallback = new HashMap<String, String>();
		deobUtil = DeofuscatorUtil.getInstance();
	}

	private boolean checkParamsType(List<Type> types, String type) {
		for (Type t : types) {
			if (t.toString().contentEquals(type))
				return true;
		}

		return false;
	}

	/**
	 * If there is a method with String type or byte we've found the method
	 * 
	 * @return
	 */
	private SootMethod searchSendMessageMethod() {
		for (SootMethod sm : mclientClass.getMethods()) {
			if (sm.getParameterCount() == 3) {
				List<Type> types = sm.getParameterTypes();
				if (checkParamsType(types, ExtraTypes.STRING_TYPE)) {
					return sm;
				}
			}
		}
		return null;

	}

	public boolean deofuscateMessageclient() {
		boolean obf = false;
		try {
			//SootMethod sm = mclientClass.getMethodByNameUnsafe("sendMessage");
			SootMethod sm = null;
			if (sm == null) {
				SootMethod sendMessageObf = searchSendMessageMethod();
				if (sendMessageObf != null) {
					obf = true;
					obfuscationMap.put(sendMessageObf.getSubSignature(), "sendMessage");
					sendMessageObf.setName("sendMessage");
				}
			}
			List<Type> params = new ArrayList<>();

			SootClass child = Scene.v().getSootClass(ExtraTypes.ON_MESSAGE_RECEIVED_LISTENER);
			if (child != null && !child.isPhantom()) {
				sm = null;
				sm = child.getMethodByNameUnsafe("onMessageReceived");
				if (sm == null) {
					params.add(Scene.v().getTypeUnsafe(ExtraTypes.MESSAGE_EVENT));
					List<SootMethod> result = deobUtil.searchMethodsByParams(child, params);
					if (result.size() == 1) {
						obf = true;
						obfuscationMapCallback.put(result.get(0).getSubSignature(), "onMessageReceived");
						result.get(0).setName("onMessageReceived");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obf;
	}
}
