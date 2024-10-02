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
    private JButton addDriverButton, editDriverButton, deleteDriverButton;
    private JTextField searchField;
    private JComboBox<String> searchTypeComboBox;

    public void show() {
        // Создание основного окна
        mainFrame = new JFrame("GAI System");
        mainFrame.setSize(800, 400);
        mainFrame.setLocation(100, 100);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Создание кнопок
        addDriverButton = new JButton("Add Driver");
        editDriverButton = new JButton("Edit Driver");
        deleteDriverButton = new JButton("Delete Driver");

        // Панель инструментов с кнопками
        JToolBar toolBar = new JToolBar("Toolbar");
        toolBar.add(addDriverButton);
        toolBar.add(editDriverButton);
        toolBar.add(deleteDriverButton);

        // Добавление панели инструментов в верхнюю часть окна
        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(toolBar, BorderLayout.NORTH);

        // Создание таблицы с данными
        String[] columns = {"Driver Name", "Car License Plate", "Inspection Date", "Violation"};
        String[][] data = {
                {"John Doe", "ABC123", "2024-01-01", "Speeding"},
                {"Jane Smith", "XYZ789", "2023-12-10", "Parking Violation"}
        };
        tableModel = new DefaultTableModel(data, columns);
        dataTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(dataTable);

        // Добавление таблицы в центральную часть окна
        mainFrame.add(scrollPane, BorderLayout.CENTER);

        // Элементы поиска
        searchTypeComboBox = new JComboBox<>(new String[]{"By Name", "By License Plate"});
        searchField = new JTextField(15);
        JButton searchButton = new JButton("Search");

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
