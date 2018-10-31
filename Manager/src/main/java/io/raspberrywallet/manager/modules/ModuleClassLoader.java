package io.raspberrywallet.manager.modules;

import com.stasbar.Logger;
import io.raspberrywallet.manager.Configuration;
import io.raspberrywallet.manager.pki.JarVerifier;
import io.raspberrywallet.manager.pki.PkiUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ModuleClassLoader {

    /**
     * @param modulesDir - directory where module classes are located
     * @return objects of instantiated Modules {@link Module}
     */
    @NotNull
    public static List<Module> getModulesFrom(File modulesDir, Configuration.ModulesConfiguration modulesConfiguration) {
        if (!modulesDir.exists()) {
            System.out.println("\"" + modulesDir.getPath() + "\" doesn't exist! Defaulting to /opt/wallet/modules");

            modulesDir = new File("/opt/wallet/modules");
            if (!modulesDir.exists() && !modulesDir.mkdirs()) {
                System.err.println("Cannot create necessary directories!");
                return Collections.emptyList();
            }
        }

        File[] files = Objects.requireNonNull(modulesDir.listFiles(), "moduleDir files can not be null");
        try {
            URL url = modulesDir.toURI().toURL();

            URLClassLoader classLoader = new URLClassLoader(new URL[]{url}, ModuleClassLoader.class.getClassLoader());

            List<Class<?>> classes = getClasses(files, classLoader);
            List<Module> modules = instantiateModulesObjects(classes, modulesConfiguration);
            printLoadedModules(modules);
            return modules;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static List<Class<?>> getClasses(File[] files, final URLClassLoader classLoader) {
        return Arrays.stream(files).map(file ->
                {
                    try {
                        if (verifyJarSignature(file)) {

                            String fileName = file.getName();

                            String packageName = "io.raspberrywallet.manager.modules." +
                                    fileName.substring(0, fileName.indexOf("Module")) // PinModule.class/.jar -> Pin
                                            .toLowerCase();

                            String className = fileName.substring(0, fileName.indexOf("."));
                            String fullClassName = packageName + "." + className;

                            Logger.info(String.format("Successfully verified module %s", fullClassName));

                            return classLoader.loadClass(fullClassName);
                        } else {
                            Logger.err(String.format("Failed to verify module %s", file.getName()));
                            return null;
                        }
                    } catch (ClassNotFoundException | IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
        ).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static boolean verifyJarSignature(File file) throws MalformedURLException {
        JarVerifier jarVerifier = new JarVerifier(file.toURI().toURL());
        try {
            jarVerifier.verify(PkiUtils.getCert());
            return true;
        } catch (SecurityException e) {
            return false;
        } catch (CertificateException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static List<Module> instantiateModulesObjects(List<Class<?>> classes, Configuration.ModulesConfiguration modulesConfiguration) {
        return classes.stream().map(clazz -> {
            try {
                Module module = (Module) clazz.getConstructor(modulesConfiguration.getClass()).newInstance(modulesConfiguration);
                Logger.info("Successfully instantiated " + module.getClass().getSimpleName());
                return module;
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                e.printStackTrace();
                return null;
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                Logger.err("Could not find constructor with ModulesConfiguration constructor parameter in class " + clazz.getName());
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static void printLoadedModules(List<Module> modules) {
        Logger.info("ModuleClassLoader", String.format("Loaded %d modules", modules.size()));
        modules.forEach(module ->
                Logger.info(String.format("Module {\n\tname: %s \n\tid: %s \n\tdescription: %s \n}",
                        module.getClass().getSimpleName(),
                        module.getId(),
                        module.getDescription()))
        );
    }
}
