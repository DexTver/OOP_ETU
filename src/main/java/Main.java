import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;

import Exceptions.*;

/**
 * Программа для работы с данными о водителях и их нарушениях.
 * Содержит функции добавления, редактирования, удаления записей, а также сохранения и загрузки данных в файл.
 *
 * @author Шарапов Иван 3312
 * @version 1.6
 */
public class Main {
    private JFrame mainFrame;
    private DefaultTableModel tableModel;
    private JTable dataTable;
    private JButton addDriverButton, editDriverButton, deleteDriverButton, loadDriverButton, saveDriverButton, generateReportButton;
    private JTextField searchField;
    private JComboBox<String> searchTypeComboBox;

    private final Object syncObject = new Object();
    private boolean isDataLoaded = false;

    /**
     * Конструктор класса Main.
     * Инициализирует основное окно приложения для работы с данными.
     */
    public Main() {
        // Конструктор по умолчанию, который инициализирует класс Main.
    }

    /**
     * Метод для создания и отображения главного окна программы.
     * Включает создание таблицы, панели инструментов с кнопками и панель поиска.
     */
    public void show() {
        // Создание основного окна приложения
        mainFrame = new JFrame("GAI System");
        mainFrame.setSize(800, 400);
        mainFrame.setLocation(100, 100);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Создание кнопок для управления записями
        addDriverButton = new JButton("Добавить");
        editDriverButton = new JButton("Редактировать");
        deleteDriverButton = new JButton("Удалить");
        loadDriverButton = new JButton("Загрузить");
        saveDriverButton = new JButton("Сохранить");
        generateReportButton = new JButton("Сформировать отчёт");

        // Панель инструментов, которая содержит кнопки
        JToolBar toolBar = new JToolBar("Toolbar");
        toolBar.setLayout(new BorderLayout()); // Устанавливаем BorderLayout для панели инструментов

        // Панель с кнопками управления (добавление, редактирование, удаление)
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(addDriverButton);
        leftPanel.add(editDriverButton);
        leftPanel.add(deleteDriverButton);
        toolBar.add(leftPanel, BorderLayout.WEST); // Размещаем в левой части панели

        // Панель с кнопками сохранения и загрузки
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(loadDriverButton);
        rightPanel.add(saveDriverButton);
        rightPanel.add(generateReportButton);
        toolBar.add(rightPanel, BorderLayout.EAST); // Размещаем в правой части панели

        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(toolBar, BorderLayout.NORTH); // Размещение панели инструментов сверху

        // Создание таблицы для отображения данных
        String[] columns = {"ФИО водителя", "Номер машины", "Дата нарушения", "Тип нарушения"};
        String[][] data = {};
        tableModel = new DefaultTableModel(data, columns);
        dataTable = new JTable(tableModel); // Таблица, которая использует данные tableModel
        JScrollPane scrollPane = new JScrollPane(dataTable); // Добавляем прокрутку для таблицы
        mainFrame.add(scrollPane, BorderLayout.CENTER); // Размещаем таблицу в центре окна

        // Элементы поиска: поле ввода и кнопка "Поиск"
        searchTypeComboBox = new JComboBox<>(new String[]{"По имени", "По номеру машины", "По дате нарушения", "По типу нарушения"});
        searchField = new JTextField(15);
        JButton searchButton = new JButton("Поиск");

        JPanel searchPanel = new JPanel();
        searchPanel.add(searchTypeComboBox);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        mainFrame.add(searchPanel, BorderLayout.SOUTH); // Панель поиска размещается внизу

        // Добавляем действия для кнопок

        // "Добавить" — действие для добавления новой записи
        addDriverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RecordDialog(mainFrame, tableModel, -1).setVisible(true); // -1 означает, что это добавление новой записи
            }
        });

        // "Редактировать" — действие для редактирования выбранной записи
        editDriverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = dataTable.getSelectedRows();

                // Проверка, что выбрана ровно одна строка
                if (selectedRows.length != 1) {
                    JOptionPane.showMessageDialog(mainFrame, "Пожалуйста, выберите только одну строку для редактирования.", "Ошибка", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Открытие диалогового окна для редактирования выбранной строки
                int selectedRow = selectedRows[0];
                new RecordDialog(mainFrame, tableModel, selectedRow).setVisible(true);
            }
        });

        // "Удалить" — действие для удаления выбранной записи
        deleteDriverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Проверяем, есть ли выделенные строки
                int[] selectedRows = dataTable.getSelectedRows();
                if (selectedRows.length == 0) {
                    JOptionPane.showMessageDialog(mainFrame, "Нет выделенных строк для удаления.", "Ошибка", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Запрос подтверждения у пользователя
                int confirm = JOptionPane.showConfirmDialog(mainFrame, "Вы уверены, что хотите удалить выделенные строки?", "Подтверждение удаления", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    // Удаляем строки с конца списка, чтобы избежать смещения индексов
                    for (int i = selectedRows.length - 1; i >= 0; i--) {
                        tableModel.removeRow(selectedRows[i]);
                    }
                    JOptionPane.showMessageDialog(mainFrame, "Выделенные строки успешно удалены.", "Удаление", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // "Поиск" — выполняет поиск в таблице, по введённой строке
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    validateSearchField(searchField);  // Проверка значений в поле поиска
                    performSearch(searchField.getText());  // Выполнение поиска по введенному тексту
                } catch (NullPointerException ex) {
                    JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                } catch (EmptySearchException ex) {
                    JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Ошибка", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // "Загрузить" — открывает диалоговое окно для выбора файла и загружает данные в таблицу
        loadDriverButton.addActionListener(e -> {
            Thread loadDataThread = createLoadDataThread();
            loadDataThread.start();
        });

        // "Сохранить" — открывает диалоговое окно для сохранения файла и записывает данные таблицы в файл
        saveDriverButton.addActionListener(e -> {
            Thread saveDataThread = createSaveDataThread();
            saveDataThread.start();
        });

        // "Сформировать отчёт" - генерирует отчёт в формате HTML
        generateReportButton.addActionListener(e -> {
            Thread generateReportThread = createGenerateReportThread();
            generateReportThread.start();
        });

        mainFrame.setVisible(true); // Делаем главное окно видимым
    }

    /**
     * Возвращает индекс столбца для поиска на основе выбранного поля.
     *
     * @param selectedField Поле, выбранное пользователем в JComboBox для поиска
     * @return Индекс столбца для поиска, или -1, если поле не распознано
     */
    private int getColumnIndex(String selectedField) {
        switch (selectedField) {
            case "По имени":
                return 0; // Индекс столбца с ФИО водителя
            case "По номеру машины":
                return 1; // Индекс столбца с номером машины
            case "По дате нарушения":
                return 2; // Индекс столбца с датой нарушения
            case "По типу нарушения":
                return 3; // Индекс столбца с типом нарушения
            default:
                return -1;
        }
    }

    /**
     * Проверяет значение в поле поиска и генерирует исключения, если поле пустое или содержит null.
     *
     * @param searchField Поле ввода текста для поиска
     * @throws EmptySearchException Если поле ввода пустое
     * @throws NullPointerException Если значение в поле null
     */
    private void validateSearchField(JTextField searchField) throws EmptySearchException, NullPointerException {
        String searchText = searchField.getText();
        if (searchText == null) {
            throw new NullPointerException("Поисковый запрос отсутствует");
        }
        if (searchText.isEmpty()) {
            throw new EmptySearchException();
        }
    }

    /**
     * Выполняет поиск по таблице и выделяет все строки, содержащие указанный текст в выбранном поле, без учета регистра.
     *
     * @param query Строка для поиска
     */
    private void performSearch(String query) {
        dataTable.clearSelection(); // Снимаем предыдущее выделение
        boolean found = false;

        // Приводим запрос к нижнему регистру
        String lowerCaseQuery = query.toLowerCase();

        // Получаем индекс столбца для поиска
        int columnIndex = getColumnIndex((String) searchTypeComboBox.getSelectedItem());
        if (columnIndex == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Некорректное поле для поиска", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Проходим по всем строкам, но только в выбранном столбце
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String cellValue = tableModel.getValueAt(i, columnIndex).toString().toLowerCase();

            // Если значение ячейки содержит искомый текст без учета регистра
            if (cellValue.contains(lowerCaseQuery)) {
                dataTable.addRowSelectionInterval(i, i); // Выделяем строку
                found = true;
            }
        }

        if (!found) {
            JOptionPane.showMessageDialog(mainFrame, "Совпадения не найдены", "Результат поиска", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private Thread createLoadDataThread() {
        return new Thread(() -> {
            synchronized (syncObject) {
                loadData(); // Загрузка данных
                isDataLoaded = true; // Флаг завершения
                syncObject.notifyAll(); // Уведомляем другие потоки
            }
        });
    }

    private Thread createSaveDataThread() {
        return new Thread(() -> {
            synchronized (syncObject) {
                try {
                    while (!isDataLoaded) {
                        syncObject.wait(); // Ждем завершения загрузки
                    }
                    saveData(); // Сохранение данных
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Thread createGenerateReportThread() {
        return new Thread(() -> {
            synchronized (syncObject) {
                try {
                    while (!isDataLoaded) {
                        syncObject.wait(); // Ждем завершения загрузки
                        // * Поставленная в лабораторной работе задача ломает логику программы
                        // Ведь отчёт генерируется по данным из таблички, а не XML
                        // Потому что пользователь может сохранить XML куда угодно
                        // А может и не сохранить совсем
                    }
                    generateHtmlReport(); // Генерация отчёта
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setDialogTitle("Открыть XML файл");
        fileChooser.setFileFilter(new FileNameExtensionFilter("XML файлы", "xml"));

        int userSelection = fileChooser.showOpenDialog(mainFrame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();

            try {
                // Читаем XML-документ
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(fileToLoad);
                doc.getDocumentElement().normalize();

                tableModel.setRowCount(0);

                NodeList driverNodes = doc.getElementsByTagName("driver");

                for (int i = 0; i < driverNodes.getLength(); i++) {
                    Node node = driverNodes.item(i);
                    NamedNodeMap attributes = node.getAttributes();

                    String name = attributes.getNamedItem("name").getNodeValue();
                    String license = attributes.getNamedItem("license").getNodeValue();
                    String violationDate = attributes.getNamedItem("violationDate").getNodeValue();
                    String violationType = attributes.getNamedItem("violationType").getNodeValue();

                    tableModel.addRow(new Object[]{name, license, violationDate, violationType});
                }

                JOptionPane.showMessageDialog(mainFrame, "Данные успешно загружены из XML файла.");
            } catch (ParserConfigurationException | SAXException | IOException ex) {
                JOptionPane.showMessageDialog(mainFrame, "Ошибка при загрузке данных из XML файла.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void saveData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setDialogTitle("Сохранить как XML");
        fileChooser.setFileFilter(new FileNameExtensionFilter("XML файлы", "xml"));

        int userSelection = fileChooser.showSaveDialog(mainFrame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getAbsolutePath().endsWith(".xml")) {
                fileToSave = new File(fileToSave + ".xml"); // Добавляем расширение .xml, если отсутствует
            }

            try {
                // Создаем XML-документ
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.newDocument();

                Element rootElement = doc.createElement("drivers");
                doc.appendChild(rootElement);

                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    Element driver = doc.createElement("driver");

                    driver.setAttribute("name", (String) tableModel.getValueAt(i, 0));
                    driver.setAttribute("license", (String) tableModel.getValueAt(i, 1));
                    driver.setAttribute("violationDate", (String) tableModel.getValueAt(i, 2));
                    driver.setAttribute("violationType", (String) tableModel.getValueAt(i, 3));

                    rootElement.appendChild(driver);
                }

                // Сохраняем XML-документ в файл
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(fileToSave);
                transformer.transform(source, result);

                JOptionPane.showMessageDialog(mainFrame, "Данные успешно сохранены в XML файл.");
            } catch (ParserConfigurationException | TransformerException ex) {
                JOptionPane.showMessageDialog(mainFrame, "Ошибка при сохранении данных в XML файл.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Генерирует HTML-отчет на основе данных из таблицы.
     */
    private void generateHtmlReport() {
        try {
            // Путь к шаблону отчета
            String jrxmlPath = "src/main/resources/GAI.jrxml";

            // Компиляция шаблона отчета
            JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlPath);

            // Подготовка данных для отчета из таблицы
            JRTableModelDataSource dataSource = new JRTableModelDataSource(tableModel);

            // Параметры для отчета (если нужны)
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("ReportTitle", "Отчет о данных ГАИ");
            parameters.put("Author", "GAI System");

            // Заполнение отчета
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // Генерация HTML-отчета
            String outputFilePath = "report.html";
            HtmlExporter exporter = new HtmlExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputFilePath));

            exporter.exportReport();

            JOptionPane.showMessageDialog(mainFrame, "HTML-отчет успешно создан: " + outputFilePath);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при создании отчета: " + e.getMessage());
        }
    }

    /**
     * Точка входа в приложение. Запускает метод show() для отображения GUI.
     *
     * @param args Аргументы командной строки (не используются).
     */
    public static void main(String[] args) {
        new Main().show(); // Запуск приложения
    }
}
