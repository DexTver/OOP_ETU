import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * @author Шарапов Иван 3312
 * @version 1.0
 */

public class Main {
    // Объявление графических компонентов
    private JFrame mainFrame;
    private DefaultTableModel tableModel;
    private JTable dataTable;
    private JButton addDriverButton, editDriverButton, deleteDriverButton, loadDriverButton, saveDriverButton;
    private JTextField searchField;
    private JComboBox<String> searchTypeComboBox;

    public void show() {
        // Создание основного окна
        mainFrame = new JFrame("GAI System");
        mainFrame.setSize(800, 400);
        mainFrame.setLocation(100, 100);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Создание кнопок
        addDriverButton = new JButton("Добавить");
        editDriverButton = new JButton("Редактировать");
        deleteDriverButton = new JButton("Удалить");
        loadDriverButton = new JButton("Загрузить");
        saveDriverButton = new JButton("Сохранить");

        // Панель инструментов с кнопками
        JToolBar toolBar = new JToolBar("Toolbar");
        toolBar.add(addDriverButton);
        toolBar.add(editDriverButton);
        toolBar.add(deleteDriverButton);
        toolBar.add(loadDriverButton);
        toolBar.add(saveDriverButton);

        // Добавление панели инструментов в верхнюю часть окна
        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(toolBar, BorderLayout.NORTH);

        // Создание таблицы с данными
        String[] columns = {"ФИО водителя", "Номер машины", "Дата нарушения", "Тип нарушения"};
        String[][] data = {
                {"Иванов Иван Иванович", "А123ВС77", "15.03.2024", "Превышение скорости"},
                {"Петров Петр Петрович", "В456МН77", "20.07.2023", "Проезд на красный свет"},
                {"Смирнова Анна Сергеевна", "С789ОР77", "05.05.2024", "Нарушение парковки"},
                {"Кузнецова Мария Александровна", "Д123ЕК77", "12.12.2023", "Отсутствие страховки"},
                {"Соколов Сергей Викторович", "Е456ТР77", "22.02.2024", "Разворот в неположенном месте"}
        };
        tableModel = new DefaultTableModel(data, columns);
        dataTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(dataTable);

        // Добавление таблицы в центральную часть окна
        mainFrame.add(scrollPane, BorderLayout.CENTER);

        // Элементы поиска
        searchTypeComboBox = new JComboBox<>(new String[]{"По имени", "По номеру машины"});
        searchField = new JTextField(15);
        JButton searchButton = new JButton("Поиск");

        // Панель поиска
        JPanel searchPanel = new JPanel();
        searchPanel.add(searchTypeComboBox);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Размещение панели поиска в нижней части окна
        mainFrame.add(searchPanel, BorderLayout.SOUTH);

        // Визуализация окна
        mainFrame.setVisible(true);
    }

    /**
     * @param args (Входных аргументов нет)
     */
    public static void main(String[] args) {
        // Создание и отображение формы
        new Main().show();
    }
}
