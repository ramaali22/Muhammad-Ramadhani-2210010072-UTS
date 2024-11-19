/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.ArrayList;
import java.util.Date;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import java.text.NumberFormat;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.text.SimpleDateFormat;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
/**
 *
 * @author User
 */
public class KeuanganPribadiForm extends javax.swing.JFrame {
    
    // Mengatur aksi-aksi untuk menu aplikasi.Menambahkan item menu dan mendefinisikan behavior untuk setiap item.
    private void setupMenuActions() {
    // Setup menu File dengan opsi keluar
    JMenuItem menuExit = new JMenuItem("Keluar");
    menuExit.addActionListener(e -> System.exit(0));
    jMenu1.add(menuExit);

    // Setup menu Import dengan opsi import dari JSON
    JMenuItem menuImportJson = new JMenuItem("Import dari JSON");
    menuImportJson.addActionListener(e -> importFromJson());
    jMenu2.add(menuImportJson);

    // Setup menu Export dengan opsi export ke JSON
    JMenuItem menuExportJson = new JMenuItem("Export ke JSON");
    menuExportJson.addActionListener(e -> exportToJson());
    jMenu3.add(menuExportJson);
    }
    
    /*
     * Mengekspor data transaksi ke file JSON.
     * Method ini akan:
     * 1. Menampilkan dialog pemilihan file
     * 2. Mengkonversi data transaksi ke format JSON
     * 3. Menyimpan file dengan indentasi yang rapi
     * 
     * @throws Exception jika terjadi kesalahan dalam proses ekspor
     */
    private void exportToJson() {
        try {
            // Inisialisasi file chooser dengan filter JSON
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));
            fileChooser.setSelectedFile(new File("transaksi_keuangan.json"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                // Memastikan file berekstensi .json
                if (!file.getName().toLowerCase().endsWith(".json")) {
                    file = new File(file.getParentFile(), file.getName() + ".json");
                }
                
                // Membuat array JSON untuk menyimpan transaksi
                JSONArray jsonArray = new JSONArray();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                // Mengkonversi setiap transaksi ke objek JSON
                for (int i = 0; i < model.getSize(); i++) {
                    Transaksi t = model.getTransaksi(i);
                    JSONObject jsonTransaksi = new JSONObject();
                    jsonTransaksi.put("tanggal", dateFormat.format(t.getTanggal()));
                    jsonTransaksi.put("keterangan", t.getKeterangan());
                    jsonTransaksi.put("jumlah", t.getJumlah());
                    jsonTransaksi.put("jenis", t.getJenis());
                    jsonArray.put(jsonTransaksi);
                }

                // Menulis ke file dengan format yang rapi
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(jsonArray.toString(2)); // indentasi 2 spasi
                }

                // Menampilkan pesan sukses
                JOptionPane.showMessageDialog(this,
                    "Data berhasil diekspor ke " + file.getName(),
                    "Ekspor Berhasil",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            // Menampilkan pesan error jika terjadi kesalahan
            JOptionPane.showMessageDialog(this,
                "Gagal mengekspor data: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    
    //Mengimpor data transaksi dari file JSON.
    //Method ini akan:
    //1. Menampilkan dialog pemilihan file
    //2. Membaca dan memparse file JSON
    //3. Menambahkan atau menggabungkan data yang diimpor
    //@throws Exception jika terjadi kesalahan dalam proses impor
    private void importFromJson() {
        try {
            // Inisialisasi file chooser dengan filter JSON
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("JSON files (*.json)", "json"));

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();

                // Membaca konten file
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }
                }

                // Parse JSON dan format tanggal
                JSONArray jsonArray = new JSONArray(content.toString());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                // Konfirmasi jika ada data existing
                if (model.getSize() > 0) {
                    int response = JOptionPane.showConfirmDialog(this,
                        "Apakah Anda ingin menggabungkan data yang diimpor dengan data yang ada?",
                        "Konfirmasi Import",
                        JOptionPane.YES_NO_CANCEL_OPTION);

                    if (response == JOptionPane.CANCEL_OPTION) {
                        return;
                    } else if (response == JOptionPane.NO_OPTION) {
                        model.clear();
                    }
                }

                // Import setiap transaksi dari JSON
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    Transaksi t = new Transaksi(
                        dateFormat.parse(obj.getString("tanggal")),
                        obj.getString("keterangan"),
                        obj.getDouble("jumlah"),
                        obj.getString("jenis")
                    );

                    model.tambahTransaksi(t);
                }

                updateSaldoLabel();
                
                // Menampilkan pesan sukses
                JOptionPane.showMessageDialog(this,
                    "Data berhasil diimpor dari " + file.getName(),
                    "Import Berhasil",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            // Menampilkan pesan error jika terjadi kesalahan
            JOptionPane.showMessageDialog(this,
                "Gagal mengimpor data: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
   
    //Kelas internal untuk merepresentasikan sebuah transaksi keuangan.
    //Menyimpan informasi detail tentang setiap transaksi seperti tanggal, 
    //keterangan, jumlah, dan jenis transaksi.
    private class Transaksi {
        private Date tanggal;       // Tanggal transaksi
        private String keterangan;  // Deskripsi transaksi
        private double jumlah;      // Nilai transaksi
        private String jenis;       // Jenis transaksi (Pemasukan/Pengeluaran)
        
        /*
         * Constructor untuk membuat objek Transaksi baru
         * 
         * @param tanggal Tanggal transaksi
         * @param keterangan Deskripsi transaksi
         * @param jumlah Nilai transaksi
         * @param jenis Jenis transaksi (Pemasukan/Pengeluaran)
         */
        public Transaksi(Date tanggal, String keterangan, double jumlah, String jenis) {
            this.tanggal = tanggal;
            this.keterangan = keterangan;
            this.jumlah = jumlah;
            this.jenis = jenis;
        }
        
        // Getter dan Setter untuk setiap field
        public Date getTanggal() { return tanggal; }
        public String getKeterangan() { return keterangan; }
        public double getJumlah() { return jumlah; }
        public String getJenis() { return jenis; }
        
        public void setTanggal(Date tanggal) { this.tanggal = tanggal; }
        public void setKeterangan(String keterangan) { this.keterangan = keterangan; }
        public void setJumlah(double jumlah) { this.jumlah = jumlah; }
        public void setJenis(String jenis) { this.jenis = jenis; }
        
        /*
         * Override toString untuk format tampilan transaksi yang rapi
         * Format: YYYY-MM-DD - Keterangan - Rp XXX.XX (Jenis)
         */
        @Override
        public String toString() {
            return String.format("%tF - %s - Rp %.2f (%s)", 
                tanggal, keterangan, jumlah, jenis);
        }

    }
    
    /*
     * Kelas internal untuk mengelola model data transaksi.
     * Mengextend DefaultListModel untuk menangani tampilan list
     * dan menambahkan fungsi perhitungan saldo.
     */
    private class TransaksiModel extends DefaultListModel<String> {
        private ArrayList<Transaksi> transaksiList;  // List untuk menyimpan transaksi
        private double saldo;                        // Saldo current
        
        /*
         * Constructor untuk membuat model transaksi baru
         */
        public TransaksiModel() {
            transaksiList = new ArrayList<>();
            saldo = 0.0;
        }
        
        /*
         * Menambahkan transaksi baru dan mengupdate saldo
         * 
         * @param t Transaksi yang akan ditambahkan
         */
        public void tambahTransaksi(Transaksi t) {
            transaksiList.add(t);
            // Update saldo berdasarkan jenis transaksi
            if (t.getJenis().equals("Pemasukan")) {
                saldo += t.getJumlah();
            } else {
                saldo -= t.getJumlah();
            }
            updateListModel();
        }
        
        /*
         * Menghapus transaksi dan mengupdate saldo
         * 
         * @param index Index transaksi yang akan dihapus
         */
        public void hapusTransaksi(int index) {
            if (index >= 0 && index < transaksiList.size()) {
                Transaksi t = transaksiList.get(index);
                // Update saldo berdasarkan jenis transaksi yang dihapus
                if (t.getJenis().equals("Pemasukan")) {
                    saldo -= t.getJumlah();
                } else {
                    saldo += t.getJumlah();
                }
                transaksiList.remove(index);
                updateListModel();
            }
        }
        
        /*
         * Mengupdate transaksi yang ada dengan data baru
         * 
         * @param index Index transaksi yang akan diupdate
         * @param newTransaksi Data transaksi baru
         */
        public void updateTransaksi(int index, Transaksi newTransaksi) {
            if (index >= 0 && index < transaksiList.size()) {
                Transaksi oldTransaksi = transaksiList.get(index);
                
                // Mengembalikan efek transaksi lama pada saldo
                if (oldTransaksi.getJenis().equals("Pemasukan")) {
                    saldo -= oldTransaksi.getJumlah();
                } else {
                    saldo += oldTransaksi.getJumlah();
                }
                
                // Menerapkan efek transaksi baru pada saldo
                if (newTransaksi.getJenis().equals("Pemasukan")) {
                    saldo += newTransaksi.getJumlah();
                } else {
                    saldo -= newTransaksi.getJumlah();
                }
                
                transaksiList.set(index, newTransaksi);
                updateListModel();
            }
        }
        
        /*
         * Mengupdate tampilan list dengan data terbaru
         */
        private void updateListModel() {
            this.clear();
            for (Transaksi t : transaksiList) {
                this.addElement(t.toString());
            }
        }
        
        /*
         * Mendapatkan saldo current
         * 
         * @return Nilai saldo terkini
         */
        public double getSaldo() {
            return saldo;
        }
        
        /*
         * Mendapatkan objek Transaksi berdasarkan index
         * 
         * @param index Index transaksi yang diinginkan
         * @return Objek Transaksi pada index tersebut
         */
        public Transaksi getTransaksi(int index) {
            return transaksiList.get(index);
        }
    }
    
    private TransaksiModel model;                // Model untuk menyimpan data transaksi
    private NumberFormat currencyFormat;         // Format mata uang
    
    /*
     * Constructor untuk form utama aplikasi.
     * Menginisialisasi komponen GUI dan setup awal.
     */
    public KeuanganPribadiForm() {
        initComponents();
        
        // Inisialisasi model
        model = new TransaksiModel();
        listTransaksi.setModel(model);

        // Format mata uang Rupiah
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

        // Menambahkan action listeners
        addActionListeners();

        // Setup menu bar
        JMenuBar menuBar = new JMenuBar();
        jMenu1 = new JMenu("File");
        jMenu2 = new JMenu("Import");
        jMenu3 = new JMenu("Export");

        menuBar.add(jMenu1);
        menuBar.add(jMenu2);
        menuBar.add(jMenu3);

        setJMenuBar(menuBar);
        // Setup menu
        setupMenuActions();

        // Mengatur posisi form di tengah layar
        setLocationRelativeTo(null);
    }
    
    /*
     * Menambahkan action listeners untuk komponen GUI
     */
     private void addActionListeners() {
        // Tambahkan list selection listener
        listTransaksi.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedIndex = listTransaksi.getSelectedIndex();
                if (selectedIndex != -1) {
                    Transaksi selected = model.getTransaksi(selectedIndex);
                    // Update form dengan data yang dipilih
                    jTanggal.setDate(selected.getTanggal());
                    txtKeterangan.setText(selected.getKeterangan());
                    txtJumlah.setText(String.valueOf(selected.getJumlah()));
                    cmboxJenis.setSelectedItem(selected.getJenis());

                    // Update saldo berdasarkan jenis transaksi
                    updateSaldoBasedOnSelection(selected);
                }
            }
        });
    }
     
    private void updateSaldoBasedOnSelection(Transaksi transaksi) {
        // Metode untuk memperbarui saldo berdasarkan jenis transaksi
        double currentSaldo = model.getSaldo();
        if (transaksi.getJenis().equals("Pengeluaran")) {
            // Jika transaksi adalah pengeluaran, kurangi saldo
            currentSaldo -= transaksi.getJumlah();
        } else {
            // Jika transaksi adalah pemasukan, tambah saldo
            currentSaldo += transaksi.getJumlah();
        }
        // Perbarui label saldo dengan format mata uang
        lblSaldo.setText("Saldo: " + currencyFormat.format(currentSaldo));
    }
    
    private void tambahData() {
        try {
            // Validasi kelengkapan dan kebenaran input form
            if (jTanggal.getDate() == null || 
                txtKeterangan.getText().trim().isEmpty() ||
                txtJumlah.getText().trim().isEmpty() ||
                cmboxJenis.getSelectedIndex() == -1) {

                // Tampilkan pesan error untuk setiap input yang tidak valid
                if (jTanggal.getDate() == null) {
                    JOptionPane.showMessageDialog(this,
                        "Tanggal harus diisi!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    jTanggal.requestFocus();
                    return;
                }

                if (txtKeterangan.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Keterangan harus diisi!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    txtKeterangan.requestFocus();
                    return;
                }

                if (txtJumlah.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Jumlah harus diisi!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    txtJumlah.requestFocus();
                    return;
                }

                return;
            }

            // Validasi format jumlah (konversi dan cek nilai positif)
            String jumlahStr = txtJumlah.getText().trim().replace(",", "");
            double jumlah = Double.parseDouble(jumlahStr);

            if (jumlah <= 0) {
                JOptionPane.showMessageDialog(this,
                    "Jumlah harus lebih besar dari 0!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                txtJumlah.requestFocus();
                return;
            }

            // Buat objek transaksi baru dengan data dari form
            Transaksi transaksi = new Transaksi(
                jTanggal.getDate(),
                txtKeterangan.getText().trim(),
                jumlah,
                (String) cmboxJenis.getSelectedItem()
            );

            // Tambahkan transaksi ke model
            model.tambahTransaksi(transaksi);
            
            // Perbarui label saldo
            updateSaldoLabel();
            
            // Bersihkan form setelah menambah data
            clearForm();

            // Tampilkan pesan sukses
            JOptionPane.showMessageDialog(this,
                "Data transaksi berhasil ditambahkan!",
                "Sukses",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            // Tangani error jika format jumlah tidak valid
            JOptionPane.showMessageDialog(this,
                "Format jumlah tidak valid! Masukkan angka saja.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            txtJumlah.requestFocus();
        } catch (Exception ex) {
            // Tangani error umum lainnya
            JOptionPane.showMessageDialog(this,
                "Terjadi kesalahan: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void hapusData() {
        // Dapatkan indeks transaksi yang dipilih
        int selectedIndex = listTransaksi.getSelectedIndex();
        if (selectedIndex != -1) {
            // Tampilkan konfirmasi penghapusan
            int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus transaksi ini?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                // Hapus transaksi dari model jika dikonfirmasi
                model.hapusTransaksi(selectedIndex);
                // Perbarui label saldo
                updateSaldoLabel();
            }
        } else {
            // Tampilkan pesan error jika tidak ada transaksi yang dipilih
            JOptionPane.showMessageDialog(this,
                "Pilih transaksi yang akan dihapus!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void ubahData() {
        // Dapatkan indeks transaksi yang dipilih
        int selectedIndex = listTransaksi.getSelectedIndex();
        if (selectedIndex != -1) {
            // Ambil data transaksi yang dipilih
            Transaksi selected = model.getTransaksi(selectedIndex);
            
            // Isi form dengan data transaksi yang dipilih
            jTanggal.setDate(selected.getTanggal());
            txtKeterangan.setText(selected.getKeterangan());
            txtJumlah.setText(String.valueOf(selected.getJumlah()));
            cmboxJenis.setSelectedItem(selected.getJenis());
            
            // Hapus data lama dari model
            model.hapusTransaksi(selectedIndex);
            
            // Beri petunjuk kepada pengguna untuk menyimpan perubahan
            JOptionPane.showMessageDialog(this,
                "Edit data dalam form dan klik 'Tambah Data' untuk menyimpan perubahan",
                "Informasi",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Tampilkan pesan error jika tidak ada transaksi yang dipilih
            JOptionPane.showMessageDialog(this,
                "Pilih transaksi yang akan diubah!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearForm() {
        // Reset semua komponen form ke kondisi awal
        jTanggal.setDate(null);
        txtKeterangan.setText("");
        txtJumlah.setText("");
        cmboxJenis.setSelectedIndex(0);
    }
    
    private void updateSaldoLabel() {
        // Perbarui label saldo dengan format mata uang
        lblSaldo.setText("Saldo: " + currencyFormat.format(model.getSaldo()));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        lblTanggal = new javax.swing.JLabel();
        jTanggal = new com.toedter.calendar.JDateChooser();
        lblKeterangan = new javax.swing.JLabel();
        txtKeterangan = new javax.swing.JTextField();
        lblJumlah = new javax.swing.JLabel();
        txtJumlah = new javax.swing.JTextField();
        lblJenis = new javax.swing.JLabel();
        cmboxJenis = new javax.swing.JComboBox<>();
        btnTambahData = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listTransaksi = new javax.swing.JList<>();
        jPanel3 = new javax.swing.JPanel();
        btnUbah = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        lblSaldo = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Aplikasi Keuangan Pribadi");
        setSize(new java.awt.Dimension(1000, 600));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Form Input Data Keuangan"));
        jPanel1.setMaximumSize(new java.awt.Dimension(200, 200));
        jPanel1.setMinimumSize(new java.awt.Dimension(300, 30));
        jPanel1.setPreferredSize(new java.awt.Dimension(500, 200));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        lblTanggal.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblTanggal.setText("Tanggal:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(7, 30, 7, 30);
        jPanel1.add(lblTanggal, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 32;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanel1.add(jTanggal, gridBagConstraints);

        lblKeterangan.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblKeterangan.setText("Keterangan:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(7, 30, 7, 30);
        jPanel1.add(lblKeterangan, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 141;
        gridBagConstraints.ipady = 7;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 6, 28);
        jPanel1.add(txtKeterangan, gridBagConstraints);

        lblJumlah.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblJumlah.setText("Jumlah (Rp):");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(7, 30, 7, 30);
        jPanel1.add(lblJumlah, gridBagConstraints);

        txtJumlah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtJumlahKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 143;
        gridBagConstraints.ipady = 7;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(10, 5, 6, 28);
        jPanel1.add(txtJumlah, gridBagConstraints);

        lblJenis.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblJenis.setText("Jenis:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        jPanel1.add(lblJenis, gridBagConstraints);

        cmboxJenis.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        cmboxJenis.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pemasukan", "Pengeluaran", " " }));
        jPanel1.add(cmboxJenis, new java.awt.GridBagConstraints());

        btnTambahData.setBackground(new java.awt.Color(153, 153, 0));
        btnTambahData.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        btnTambahData.setText("Tambah Data");
        btnTambahData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahDataActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.ipadx = 150;
        gridBagConstraints.ipady = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.8;
        gridBagConstraints.insets = new java.awt.Insets(7, 5, 7, 5);
        jPanel1.add(btnTambahData, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Riwayat Keuangan"));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setMinimumSize(new java.awt.Dimension(50, 50));

        listTransaksi.setAlignmentX(10.0F);
        listTransaksi.setAlignmentY(10.0F);
        jScrollPane1.setViewportView(listTransaksi);

        jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);
        jPanel2.getAccessibleContext().setAccessibleName("Riwayat Keuangan");

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        btnUbah.setBackground(new java.awt.Color(51, 153, 255));
        btnUbah.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        btnUbah.setText("Ubah");
        btnUbah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUbahActionPerformed(evt);
            }
        });
        jPanel3.add(btnUbah);

        btnHapus.setBackground(new java.awt.Color(255, 0, 0));
        btnHapus.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        btnHapus.setText("Hapus");
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });
        jPanel3.add(btnHapus);

        lblSaldo.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
        lblSaldo.setText("Saldo: Rp 0");
        lblSaldo.setToolTipText("");
        lblSaldo.setFocusable(false);
        lblSaldo.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jPanel3.add(lblSaldo);

        getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Import");
        jMenuBar1.add(jMenu2);

        jMenu3.setText("Export");
        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnTambahDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahDataActionPerformed
        // Metode untuk menangani event klik tombol "Tambah Data"
        // Memproses penambahan riwayat keuangan baru ke dalam daftar
        tambahData();
    }//GEN-LAST:event_btnTambahDataActionPerformed

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        // Metode untuk menangani event klik tombol "Hapus"
        // Menghapus riwayat keuangan yang dipilih dari daftar
        hapusData();
    }//GEN-LAST:event_btnHapusActionPerformed

    private void btnUbahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUbahActionPerformed
        // Metode untuk menangani event klik tombol "Ubah"
        // Mempersiapkan riwayat keuangan yang dipilih untuk diedit
        ubahData();
    }//GEN-LAST:event_btnUbahActionPerformed

    private void txtJumlahKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtJumlahKeyTyped
        // Validasi input hanya memperbolehkan karakter angka
        char c = evt.getKeyChar();
        if (!Character.isDigit(c)) {
            evt.consume();  // Batalkan karakter jika bukan angka
        }
    }//GEN-LAST:event_txtJumlahKeyTyped

    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(KeuanganPribadiForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(KeuanganPribadiForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(KeuanganPribadiForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(KeuanganPribadiForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new KeuanganPribadiForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnTambahData;
    private javax.swing.JButton btnUbah;
    private javax.swing.JComboBox<String> cmboxJenis;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private com.toedter.calendar.JDateChooser jTanggal;
    private javax.swing.JLabel lblJenis;
    private javax.swing.JLabel lblJumlah;
    private javax.swing.JLabel lblKeterangan;
    private javax.swing.JLabel lblSaldo;
    private javax.swing.JLabel lblTanggal;
    private javax.swing.JList<String> listTransaksi;
    private javax.swing.JTextField txtJumlah;
    private javax.swing.JTextField txtKeterangan;
    // End of variables declaration//GEN-END:variables
}
