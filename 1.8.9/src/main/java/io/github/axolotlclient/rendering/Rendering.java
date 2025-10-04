package io.github.axolotlclient.rendering;


import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.ClientModInitializer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

@Slf4j
public class Rendering implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ExRenderPipelines.init();
	}

	//private static final StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

	public static void logGLError(String desc) {
		int i = GL11.glGetError();
		if (i != 0) {
			/*var desc = walker.walk(s -> s.skip(1).limit(2)
				.map(f -> "%s#%s%s::%s"
					.formatted(f.getClassName(), f.getMethodName(), f.getDescriptor(), f.getLineNumber())
				).collect(Collectors.joining("\n")));*/
			log.error("########## GL ERROR ##########");
			log.error("@ {}", desc);
			log.error("{}: {}", i, GLU.gluErrorString(i));
		}
	}
}
