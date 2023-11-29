package github.zimoyin.mtool.util;

import lombok.Getter;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 解析类以获取类上的注解
 */
public class StateClassVisitorUtil {

    private StateClassVisitorUtil() {
    }

    /**
     * 获取类上的注解
     *
     * @param className 类名称
     * @param paths     jar 的路径
     * @return class 上的注解
     */
    public static List<String> getClassAnnotations(String className, List<String> paths) throws IOException {
        ClassReader classReader = new ClassReader(Objects.requireNonNull(ClassReaderUtil.readClassBytes(className, paths.toArray(new String[0]), false)));
        ClassVisitor visitor = new ClassVisitor();
        classReader.accept(visitor, 0);
        return visitor.getClassAnnotations();
    }


    /**
     * 获取类上的父类
     *
     * @param className 类名称
     * @param paths     jar 的路径
     * @return class 的父类
     */
    @Deprecated
    public static List<String> getSuperClass(String className, List<String> paths) throws IOException {
        ClassReader classReader = new ClassReader(Objects.requireNonNull(ClassReaderUtil.readClassBytes(className, paths.toArray(new String[0]), false)));
        ClassVisitor visitor = new ClassVisitor();
        classReader.accept(visitor, 0);
        return visitor.getSuperClass();
    }

    /**
     * 获取类上的父类
     *
     * @param className 类名称
     * @return class 的父类
     */
    public static ClassVisitor getVisitor(String className) throws IOException {
        return getVisitor(className, null);
    }

    /**
     * 获取Visitor
     *
     * @param className 类名称
     * @param paths     jar 的路径
     * @return class 的父类
     */
    public static ClassVisitor getVisitor(String className, List<String> paths) throws IOException {
        ClassReader classReader;
        if (paths == null || paths.size() == 0) classReader = new ClassReader(className);
        else
            classReader = new ClassReader(Objects.requireNonNull(ClassReaderUtil.readClassBytes(className, paths.toArray(new String[0]), false)));
        ClassVisitor visitor = new ClassVisitor();
        classReader.accept(visitor, 0);
        return visitor;
    }

    @Getter
    public static class ClassVisitor extends org.objectweb.asm.ClassVisitor {
        private final List<String> classAnnotations = new ArrayList<>();
        private final List<String> superClass = new ArrayList<>();

        public ClassVisitor() {
            super(Opcodes.ASM9);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            String annotationName = Type.getType(desc).getClassName();
            classAnnotations.add(annotationName);
            return super.visitAnnotation(desc, visible);
        }


        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            superClass.add(superName.replace("/", "."));
            super.visit(version, access, name, signature, superName, interfaces);
        }
    }
}
