import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Сохранение и загрузка объекта (текстовый файл)
 */
public class FileStorage {
    static Map<Class, Class> types = new HashMap<>();

    static {
        types.put(Byte.TYPE, Byte.class);
        types.put(Short.TYPE, Short.class);
        types.put(Integer.TYPE, Integer.class);
        types.put(Long.TYPE, Long.class);
        types.put(Float.TYPE, Float.class);
        types.put(Double.TYPE, Double.class);
        types.put(Boolean.TYPE, Boolean.class);
        types.put(Character.TYPE, Character.class);
    }

    /**
     * Сохранение в файл
     *
     * @param obj      объект
     * @param fileName имя файла
     */
    public static void save(Object obj, String fileName) throws FileNotFoundException, IllegalAccessException {
        try (PrintWriter wr = new PrintWriter(fileName)) {
            Class cl = obj.getClass();
            // Записываем имя класса
            wr.println(cl.getCanonicalName());
            // Цикл по полям класса
            for (Field field : cl.getDeclaredFields()) {
                field.setAccessible(true);
                wr.println(field.getName() + " = " + field.get(obj));
            }
        }
    }

    /**
     * Загрузка из файла
     *
     * @param fileName имя файла
     */
    public static Object load(String fileName) throws Exception {
        try (Scanner scanner = new Scanner(new File(fileName))) {
            // Считываем имя класса
            String className = scanner.nextLine();
            // Получаем класс по имени
            Class cl = Class.forName(className);
            // Создаём экземпляр класса
            Object obj = cl.newInstance();
            // Чтение до конца файла
            while (scanner.hasNext()) {
                String fieldName = scanner.next().trim();
                String eq = scanner.next(); // skip
                String valueStr = scanner.nextLine().trim();
                // Получаем у класса поле по имени
                Field field = cl.getDeclaredField(fieldName);
                // Делаем доступными private/protected/package local поля
                field.setAccessible(true);
                setValue(obj, field, valueStr);
            }
            return obj;
        }
    }

    private static void setValue(Object obj, Field f, String s) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        //if(f.getType().equals(Integer.TYPE))
        //    f.setInt(obj, Integer.valueOf(s));
        Class cls = types.get(f.getType());
        if (f.getType().equals(Character.TYPE)) {
            f.setChar(obj, s.charAt(0));
        } else {
            Method valueOf = cls.getMethod("valueOf", String.class);
            Object value = valueOf.invoke(null, s);
            f.set(obj, value);
        }
    }
}
