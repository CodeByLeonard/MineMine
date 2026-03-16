package edu.shch.jdk;

import sun.misc.Unsafe;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.ClassTransform;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("unused")
public class Agent {
    private static void error(String msg) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String currentTime = LocalTime.now().format(formatter);
        System.out.printf("[%s ERROR]: %s%n", currentTime, msg);
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        patchUnsafe(inst);
    }

    private static void patchUnsafe(Instrumentation inst) {
        try (var in = Unsafe.class.getResourceAsStream("/sun/misc/Unsafe.class")) {
            if (in != null) {
                ClassModel classModel = ClassFile.of().parse(in.readAllBytes());
                byte[] quietClass = ClassFile.of().transformClass(
                    classModel,
                    ClassTransform.transformingMethodBodies(
                        model -> model.methodName().stringValue().equals("trySetMemoryAccessWarned"),
                        (builder, element) -> builder.bipush(0).ireturn()
                    )
                );
                Class<?> unsafeClass = Unsafe.class;
                ClassDefinition def = new ClassDefinition(unsafeClass, quietClass);
                inst.redefineClasses(def);
            } else {
                error("Could not load Unsafe.");
            }
        } catch (IOException e) {
            error("Failed to STFU Unsafe.");
        } catch (UnmodifiableClassException e) {
            error("Failed to Modify Class");
        } catch (ClassNotFoundException e) {
            error("Failed to Find Unsafe.");
        }
    }
}
