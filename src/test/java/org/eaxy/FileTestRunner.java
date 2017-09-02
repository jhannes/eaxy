package org.eaxy;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class FileTestRunner extends Suite {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Directory {

        String[] value();

        String extension() default "";

    }

    public static class TestRunner extends BlockJUnit4ClassRunner {

        private final File file;

        public TestRunner(Class<?> testClass, File file) throws InitializationError {
            super(testClass);
            this.file = file;
        }

        @Override
        protected void validateConstructor(List<Throwable> errors) {
            validateOnlyOneConstructor(errors);
        }


        @Override
        public Object createTest() throws Exception {
            return getTestClass().getOnlyConstructor().newInstance(file);
        }


        @Override
        protected String getName() {
            return file.getPath();
        }

        @Override
        protected String testName(final FrameworkMethod method) {
            return String.format("%s[%s]", method.getName(), file.getName());
        }
    }

    public FileTestRunner(Class<?> testClass) throws Throwable {
        super(testClass, runners(testClass));
    }

    private static List<Runner> runners(Class<?> testClass) throws InitializationError {
        List<Runner> runners = new ArrayList<Runner>();
        Directory directoryAnnotation = testClass.getAnnotation(Directory.class);
        if (directoryAnnotation == null) {
            throw new InitializationError(testClass + " must annotation " + Directory.class.getName());
        }
        for (String value : directoryAnnotation.value()) {
            File directory = new File(value);
            if (!directory.isDirectory() && directoryAnnotation.value().length <= 1) {
                throw new InitializationError(directory + " must be a directory");
            }
            for (File file : directory.listFiles()) {
                if (file.isFile() && file.getName().endsWith(directoryAnnotation.extension())) {
                    runners.add(new TestRunner(testClass, file));
                }
            }
        }
        if (runners.isEmpty()) {
            throw new InitializationError("No files in " + Arrays.asList(directoryAnnotation.value()));
        }
        return runners;
    }

}