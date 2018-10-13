package io.raspberrywallet.manager.modules;

import com.stasbar.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
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
    public static List<Module> getModulesFrom(File modulesDir) {
        if (!modulesDir.exists()) {
            System.out.println("\""+modulesDir.getPath()+"\" doesn't exist! Defaulting to /opt/wallet/modules");
            modulesDir = new File("/opt/wallet/modules");
            if(!modulesDir.exists() && !modulesDir.mkdirs()) {System.err.println("Cannot create necessary directories!"); return Collections.emptyList();}
        }
        File[] files = Objects.requireNonNull(modulesDir.listFiles(), "moduleDir files can not be null");
        try {
            URL url = modulesDir.toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
            List<Class<?>> classes = getClasses(files, classLoader);
            List<Module> modules = instaniateModulesObjects(classes);
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
                        String className = "io.raspberrywallet.manager.modules." + file.getName().replace(".class", "");
                        return classLoader.loadClass(className);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
        ).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private static List<Module> instaniateModulesObjects(List<Class<?>> classes) {
        return classes.stream().map(clazz -> {
            try {
                return (Module) clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
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
