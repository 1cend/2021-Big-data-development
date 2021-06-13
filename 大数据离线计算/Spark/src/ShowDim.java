import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.util.Vector;

class ShowDim extends JFrame implements ActionListener{
    JFrame frame;
    JPanel panel;
    JLabel label;
    JButton searchButton;
    JTextField textField;
    public ShowDim(String[][] str){
        frame = new JFrame("表结构");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Container contentPane = frame.getContentPane();
        frame.setLocationRelativeTo(null);
        frame.setSize(1000,600);
        panel = new JPanel();
        panel.setLayout(new FlowLayout());//设置为流式布局
        textField = new JTextField(23);//设置文本框的长度
        searchButton = new JButton("查询");
        searchButton.addActionListener(this);//监听事件

        //定义一维数据作为列标题
        Object[] columnTitle = {"columnName" , "columnType" , "comment"};
        JTable table = new JTable(str, columnTitle); // 用双数组创建Table对象
        JTableHeader head = table.getTableHeader(); // 创建表格标题对象
        head.setPreferredSize(new Dimension(head.getWidth(), 25));// 设置表头大小
        head.setFont(new Font("仿宋",Font.PLAIN,20));
        JScrollPane scrollPane = new JScrollPane(table);

        //table = new JTable(tableData , columnTitle);
        table.setFont(new Font("楷体", Font.PLAIN, 18));
        table.setRowHeight(50);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);// 以下设置表格列宽
        for (int i = 0; i < 3; i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(200);
            }
            if(i!=0) {
                column.setPreferredWidth(300);
            }
        }


        panel.add(new JScrollPane(table,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        panel.setBackground(Color.white);
        panel.add(textField);
        panel.add(searchButton);

        frame.add(panel);//实现面板panel

        frame.setVisible(true);//设置可见
    }
    @SuppressWarnings("deprecation")
    @Override
    public void actionPerformed(ActionEvent e) {//处理事件
        // TODO Auto-generated method stub
        if (e.getSource()==searchButton) {
            String sql=textField.getText();
            frame.dispose();
            Llogin.sqlquery(sql);
        }
    }
}
