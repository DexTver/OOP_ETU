/**
 * @author Шарапов Иван 3312
 * @version 1.0
 */

public class FirstProg {
    /**
     * @param args (Входных аргументов нет)
     */
    public static void main(String[] args) {
        /* Исходный массив данных */
        int[] a = {-234, 567, -890, 432, 12, -678, 999, -345};
        /* Вспомогательный элемент для сортировки */
        int x;

        /* Выводим исходный массив */
        for (int i : a) {
            System.out.printf("%d ", i);
        }

        /* Сортировка массива */
        for (int i = 0; i < a.length; i++) {
            for (int j = i + 1; j < a.length; j++) {
                /* Меняем элементы местами */
                if (a[i] < a[j]) {
                    x = a[i];
                    a[i] = a[j];
                    a[j] = x;
                }
            }
        }

        System.out.print("-> ");
        /* Выводим отсортированный массив */
        for (int i : a) {
            System.out.printf("%d ", i);
        }
    }
}
