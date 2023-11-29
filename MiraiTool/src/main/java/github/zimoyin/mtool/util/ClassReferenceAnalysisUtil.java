package github.zimoyin.mtool.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 类引用分析
 * 注意：标准库在此的意思为，可以被JDK实现的类加载器加载到的类就称之为在标准库中的类。
 * 如果类只能被自定义类加载器加载，就不能被称之为在标准库中的类
 */
public class ClassReferenceAnalysisUtil {

    private ClassReferenceAnalysisUtil() {
    }

    /**
     * 类可达性分析，分析某一类是否可以被加载到
     *
     * @param className 类名称，是否可以被加载
     * @param paths     类来自的位置，可以为null。为null就只从标准库查找类
     */
    public static boolean ClassesAccessibilityAnalysis(String className, List<String> paths) {
        try {
            byte[] bytes = ClassReaderUtil.readClassBytes(className, paths.toArray(new String[0]), true);
            if (bytes.length == 0) return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 类引用可达性分析，分析某一类的引用是否可以被加载到
     *
     * @param className 类名称，是否可以被加载
     * @param paths     类来自的位置，可以为null。为null就只从标准库查找类
     */
    public static boolean ClsRefAccAnalysis(String className, List<String> paths) {
        if (paths == null) paths = new ArrayList<String>();
        HashSet<String> hashSet = new HashSet<>();
        try {
            List<String> reference = getClassReference(className, paths);
            for (String cls : reference) {
//                if (className.equals("github.zimoyin.mtool.command.filter.impl.Level"))
//                    System.out.println("检测类: " + cls);
                if (hashSet.contains(cls)) continue;
                hashSet.add(cls);
                boolean isAccessibility = ClassesAccessibilityAnalysis(cls, paths);
//                if (!isAccessibility) {
//                    System.out.println(cls);
//                }
                if (!isAccessibility) return false;
//                System.out.println("  可达 ");
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    /**
     * 获取类引用
     *
     * @param className 类名
     * @param paths     jar路径
     */
    public static List<String> getClassReference(String className, List<String> paths) throws IOException {
        ClassReader classReader = new ClassReader(Objects.requireNonNull(ClassReaderUtil.readClassBytes(className, paths.toArray(new String[0]), true)));
        ClassReferenceVisitor visitor = new ClassReferenceVisitor();
        classReader.accept(visitor, ClassReader.EXPAND_FRAMES);
        return visitor.getClassSources();
    }

    /**
     * 获取类引用
     *
     * @param className 类名
     */
    public static List<String> getClassReference(String className) throws IOException {
        ClassReader classReader = new ClassReader(className);
        ClassReferenceVisitor visitor = new ClassReferenceVisitor();
        classReader.accept(visitor, ClassReader.EXPAND_FRAMES);
        return visitor.getClassSources();
    }

    /**
     * 获取类引用
     *
     * @param cls 类
     */
    public static List<String> getClassReference(byte[] cls) throws IOException {
        ClassReader classReader = new ClassReader(cls);
        ClassReferenceVisitor visitor = new ClassReferenceVisitor();
        classReader.accept(visitor, ClassReader.EXPAND_FRAMES);
        return visitor.getClassSources();
    }

    @Slf4j
    protected static class ClassReferenceVisitor extends ClassVisitor {
        private final HashSet<String> classSources = new HashSet<>();

        public ClassReferenceVisitor() {
            super(Opcodes.ASM9);
        }

        public HashSet<String> getClassSourcesR() throws IOException {
            return classSources;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            classSources.add(name);
            classSources.add(superName);
//            log.debug("Class: " + name + " extends: " + superName);
            for (String iface : interfaces) {
                classSources.add(iface);
//                log.debug("Implements: " + iface);
            }
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
//            log.debug("Annotation: " + desc);
            classSources.add(desc.substring(1));
            return super.visitAnnotation(desc, visible);
        }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            Type type = Type.getType(desc);
//            log.debug("Field: " + name + " Type: " + type.getClassName());
            classSources.add(type.getClassName());
            return super.visitField(access, name, desc, signature, value);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            String returnType = desc.replaceAll("\\(.*\\).", "").trim();
            if (!returnType.isEmpty()) {
                if (returnType.startsWith("L")) returnType = returnType.replaceFirst("L", "").trim();
                returnType = returnType.replaceAll("\\(.*\\).", "").trim();
//                log.debug("return Type: " + returnType);
                classSources.add(returnType);
            }

            Type[] argTypes = Type.getArgumentTypes(desc);
//            log.debug("Method: " + name + " Args: " + argTypes.length);
            for (Type argType : argTypes) {
//                log.debug("Arg Type: " + argType.getClassName().replaceAll("[\\[|\\]]", ""));
                classSources.add(argType.getClassName().replaceAll("[\\[|\\]]", ""));
            }
            if (exceptions != null) for (String exception : exceptions) {
//                log.debug("exception: " + exception.replaceAll("[\\[|\\]]", ""));
                classSources.add(exception.replaceAll("[\\[|\\]]", ""));
            }
            return new MyMethodVisitor(classSources);
        }

        public List<String> getClassSources() {

            return classSources.stream()
                    .filter(Objects::nonNull)
                    .map(s -> s.replaceAll("[\\[|\\]]", ""))
                    .map(s -> s.replaceAll("[\\(|\\)]", ""))
                    .map(s -> s.replaceAll("/", "."))
                    .map(s -> s.replaceAll(";", ""))
                    .map(s -> {
                        if (s.startsWith("L")) return s.substring(1);
                        return s;
                    })
                    .filter(s -> !s.equals("int"))
                    .filter(s -> !s.equals("I"))
                    .filter(s -> !s.equals("long"))
                    .filter(s -> !s.equals("L"))
                    .filter(s -> !s.equals("char"))
                    .filter(s -> !s.equals("C"))
                    .filter(s -> !s.equals("double"))
                    .filter(s -> !s.equals("D"))
                    .filter(s -> !s.equals("float"))
                    .filter(s -> !s.equals("F"))
                    .filter(s -> !s.equals("boolean"))
                    .filter(s -> !s.equals("B"))
                    .filter(s -> !s.equals("byte"))
                    .collect(Collectors.toList());
        }

        private static class MyMethodVisitor extends MethodVisitor {
            @Getter
            private final HashSet<String> classSources;

            public MyMethodVisitor(HashSet<String> hashSet) {
                super(Opcodes.ASM9);
                this.classSources = hashSet;
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
//                log.debug("Method Annotation: " + desc.substring(1));
                classSources.add(desc.substring(1));
                return super.visitAnnotation(desc, visible);
            }

            @Override
            public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
//                log.debug("Parameter Annotation: " + desc.substring(1));
                classSources.add(desc.substring(1));
                return super.visitParameterAnnotation(parameter, desc, visible);
            }

            //方法内部的变量
            @Override
            public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                Type type = Type.getType(desc);
//                log.debug("Local Variable: " + name + " Type: " + type.getClassName().replaceAll("[\\[|\\]]", ""));
                classSources.add(type.getClassName().replaceAll("[\\[|\\]]", ""));
                super.visitLocalVariable(name, desc, signature, start, end, index);
            }
        }
    }
}
