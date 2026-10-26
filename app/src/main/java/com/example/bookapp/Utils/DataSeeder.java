package com.example.bookapp.Utils;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CHỈ DÙNG TẠM ĐỂ NHẬP DỮ LIỆU 49 CUỐN SÁCH THẬT (có ảnh bìa từ Supabase)
 * VÀO FIRESTORE - KHÔNG PHẢI CODE CHÍNH THỨC CỦA APP.
 *
 * Cách dùng: gọi BookImportSeeder.seedFullCatalog() đúng 1 LẦN (gắn tạm vào
 * 1 dòng trong onCreate() của LoginActivity hoặc 1 nút debug), chạy app 1 lần,
 * kiểm tra Firestore Console thấy đủ dữ liệu thì XÓA lời gọi đó đi ngay -
 * không để lại trong bản nộp, tránh mỗi lần mở app lại tạo trùng dữ liệu.
 *
 * Cấu trúc URL ảnh (đã xác nhận đúng với cách bạn upload lên Supabase):
 * .../DL_SACH/{category_slug}/{ten_file_goc_khong_duoi}/{ten_file_goc}.{ext}
 *
 * Giá tiền/tồn kho/năm xuất bản là dữ liệu MẪU để test giao diện, mô tả
 * sách do AI viết tóm tắt ngắn gọn (không sao chép từ bìa sách/NXB nào).
 * Bạn nên tự chỉnh lại giá và mô tả cho đúng ý đồ án trước khi báo cáo.
 */
public class DataSeeder {

    private static final String IMG_BASE =
            "https://lgwiftzrrebphjymvbbc.supabase.co/storage/v1/object/public/DL_SACH";

    // ============================================================
    // 1. ĐỊNH NGHĨA DỮ LIỆU THÔ - 6 CATEGORY
    // ============================================================
    private static class CategorySeed {
        String name, slug, iconFile;
        CategorySeed(String name, String slug, String iconFile) {
            this.name = name;
            this.slug = slug;
            this.iconFile = iconFile;
        }
    }

    private static final List<CategorySeed> CATEGORIES = Arrays.asList(
            new CategorySeed("Khoa học viễn tưởng", "khoa_hoc_vien_tuong", "ic_khoa_hoc.png"),
            new CategorySeed("Phiêu lưu", "phieu_luu", "ic_phieu_luu.jpg"),
            new CategorySeed("Thiếu nhi", "thieu_nhi", "ic_thieu_nhi.jpg"),
            new CategorySeed("Trinh thám", "trinh_tham", "ic_trinh_tham.jpg"),
            new CategorySeed("Tình cảm", "tinh_cam", "ic_tinh_cam.png"),
            new CategorySeed("Văn học kinh điển", "van_hoc_kinh_dien", "ic_van_hoc.png")
    );

    // ============================================================
    // 2. ĐỊNH NGHĨA DỮ LIỆU THÔ - 49 SÁCH
    // ============================================================
    private static class BookSeed {
        String title, author, categorySlug, imgFile, description;
        double price, salePrice;
        int stock, year;
        List<String> tags;
        boolean featured;

        BookSeed(String title, String author, String categorySlug, String imgFile,
                 double price, double salePrice, int stock, int year,
                 String description, boolean featured, String... tags) {
            this.title = title;
            this.author = author;
            this.categorySlug = categorySlug;
            this.imgFile = imgFile;
            this.price = price;
            this.salePrice = salePrice;
            this.stock = stock;
            this.year = year;
            this.description = description;
            this.featured = featured;
            this.tags = Arrays.asList(tags);
        }
    }

    private static final List<BookSeed> BOOKS = Arrays.asList(

            // ===== Khoa học viễn tưởng =====
            new BookSeed("Biên Niên Sử Narnia", "C.S. Lewis", "khoa_hoc_vien_tuong", "bien_nien_su_narnia.webp",
                    139000, 119000, 40, 1950,
                    "Bộ truyện fantasy kinh điển kể về cuộc phiêu lưu của các em nhỏ tại vùng đất huyền bí Narnia. Xuyên qua cánh cửa tủ áo bí ẩn, các em nhỏ nhà Pevensie bước vào một thế giới nơi loài vật biết nói và phép thuật ngự trị. Ở đó, họ phải đối đầu với Bạch Phù Thủy độc ác và sát cánh cùng sư tử Aslan để giành lại công lý cho vùng đất. Qua bảy tập truyện, series khắc họa hành trình trưởng thành, lòng dũng cảm và tình anh em giữa hiện thực và thế giới tưởng tượng.",
                    false, "classic"),
            new BookSeed("Chúa Tể Những Chiếc Nhẫn", "J.R.R. Tolkien", "khoa_hoc_vien_tuong", "chua_te_nhung_chiec_nhan.webp",
                    189000, 169000, 30, 1954,
                    "Sử thi fantasy về hành trình tiêu hủy chiếc nhẫn quyền lực để cứu vùng đất Trung Địa. Hobbit Frodo Baggins cùng Đoàn Hộ Nhẫn phải vượt qua muôn vàn hiểm nguy để mang chiếc Nhẫn Chúa tới núi lửa Doom, nơi duy nhất có thể tiêu diệt nó. Trên đường đi, tình bạn giữa các chủng tộc người, tiên, người lùn và hobbit được thử thách trước bóng tối của Chúa tể Sauron. Tác phẩm đặt nền móng cho toàn bộ thể loại fantasy hiện đại với thế giới Trung Địa đồ sộ và giàu chi tiết.",
                    true, "bestseller", "classic"),
            new BookSeed("Frankenstein", "Mary Shelley", "khoa_hoc_vien_tuong", "frankenstein.webp",
                    79000, 79000, 35, 1818,
                    "Câu chuyện kinh dị kinh điển về nhà khoa học tạo ra sinh vật sống và cái giá phải trả cho tham vọng. Chàng sinh viên Victor Frankenstein khát khao chinh phục giới hạn của sự sống đã tạo ra một sinh vật từ các bộ phận thi thể ghép lại. Bị chính người tạo ra mình ruồng bỏ vì ngoại hình gớm ghiếc, sinh vật dần trở nên oán hận và tìm cách trả thù. Tác phẩm đặt ra những câu hỏi sâu sắc về đạo đức khoa học, trách nhiệm của người sáng tạo và bản chất của cái thiện, cái ác.",
                    false, "classic"),
            new BookSeed("Harry Potter", "J.K. Rowling", "khoa_hoc_vien_tuong", "harrypotter.webp",
                    159000, 139000, 60, 1997,
                    "Hành trình trưởng thành của cậu bé phù thủy Harry tại trường Hogwarts. Khi biết mình là một phù thủy, Harry rời khỏi cuộc sống tẻ nhạt bên nhà Dursley để theo học tại trường Hogwarts đầy phép thuật. Cùng hai người bạn thân Ron và Hermione, cậu dần khám phá bí mật về cái chết của cha mẹ mình và mối liên hệ định mệnh với chúa tể hắc ám Voldemort. Bộ truyện gồm bảy phần theo chân Harry trưởng thành qua từng năm học, đối mặt với tình bạn, mất mát và cuộc chiến giữa thiện và ác.",
                    true, "bestseller"),
            new BookSeed("Người Về Từ Sao Hỏa", "Andy Weir", "khoa_hoc_vien_tuong", "nguoi_ve_tu_sao_hoa.jpg",
                    99000, 89000, 25, 2011,
                    "Câu chuyện sinh tồn đầy trí tuệ của một phi hành gia bị bỏ lại một mình trên Sao Hỏa. Sau một cơn bão cát khiến đồng đội tưởng anh đã chết, phi hành gia Mark Watney phải tự mình xoay xở để sống sót trên hành tinh đỏ hoang vu. Bằng kiến thức khoa học và óc hài hước, anh tìm cách trồng trọt, liên lạc với Trái Đất và chờ đợi cơ hội được giải cứu. Câu chuyện là lời ca ngợi tinh thần con người và sức mạnh của lý trí trước nghịch cảnh khắc nghiệt nhất.",
                    false, "new"),
            new BookSeed("Trò Chơi Vương Quyền", "George R.R. Martin", "khoa_hoc_vien_tuong", "tro_choi_vuong_quyen.webp",
                    179000, 159000, 28, 1996,
                    "Cuộc tranh giành ngai vàng đầy mưu mô giữa các gia tộc tại vùng đất Westeros. Tại vùng đất Westeros, các gia tộc lớn như Stark, Lannister và Targaryen không ngừng tranh đoạt quyền lực trong khi mối đe dọa từ phương Bắc xa xôi đang lớn dần. Những âm mưu chính trị, phản bội và liên minh đan xen tạo nên một thế giới đầy bất ngờ nơi không ai thực sự an toàn. Tác phẩm nổi tiếng với lối xây dựng nhân vật phức tạp và cốt truyện khó đoán, thách thức mọi định kiến về thể loại fantasy truyền thống.",
                    false, "bestseller"),
            new BookSeed("Xứ Cát", "Frank Herbert", "khoa_hoc_vien_tuong", "xu_cat.webp",
                    149000, 149000, 22, 1965,
                    "Sử thi khoa học viễn tưởng về chính trị, tôn giáo và sinh thái trên hành tinh sa mạc Arrakis. Paul Atreides, người thừa kế của Gia tộc Atreides, buộc phải chạy trốn vào sa mạc sau khi gia đình bị phản bội và thảm sát. Giữa những người Fremen bản địa, anh dần khám phá sức mạnh tiềm ẩn của mình và định mệnh gắn liền với loại gia vị quý giá nhất vũ trụ. Tác phẩm được xem là một trong những cột mốc quan trọng nhất của dòng khoa học viễn tưởng, ảnh hưởng sâu rộng đến văn hóa đại chúng sau này.",
                    false, "classic"),
            new BookSeed("Đấu Trường Sinh Tử", "Suzanne Collins", "khoa_hoc_vien_tuong", "dau_truong_sinh_tu.webp",
                    109000, 95000, 45, 2008,
                    "Cuộc chiến sinh tồn tàn khốc của các thiếu niên trong một xã hội phản địa đàng. Katniss Everdeen tình nguyện thế chỗ em gái để tham gia Đấu Trường Sinh Tử, nơi các thiếu niên buộc phải chiến đấu đến chết để giải trí cho tầng lớp thống trị. Giữa lằn ranh sinh tử, cô dần trở thành biểu tượng phản kháng cho những vùng đất bị áp bức. Câu chuyện khắc họa sự tàn khốc của quyền lực độc tài và sức mạnh của lòng dũng cảm cá nhân trong việc thay đổi vận mệnh cả một xã hội.",
                    true, "bestseller"),

            // ===== Phiêu lưu =====
            new BookSeed("Ba Người Lính Ngự Lâm", "Alexandre Dumas", "phieu_luu", "ba_nguoi_linh.webp",
                    99000, 99000, 20, 1844,
                    "Câu chuyện phiêu lưu, tình bạn và danh dự của các chàng ngự lâm quân Pháp thế kỷ 17. Chàng trai trẻ D'Artagnan rời quê nhà lên Paris lập nghiệp và nhanh chóng kết thân với ba ngự lâm quân nổi tiếng Athos, Porthos và Aramis. Cùng nhau, họ dấn thân vào những âm mưu chốn cung đình để bảo vệ danh dự hoàng hậu trước sự gian trá của Hồng y Richelieu. Tác phẩm nổi bật với tinh thần đoàn kết và trung nghĩa giữa các ngự lâm quân, trở thành biểu tượng bất hủ của tình bạn trong văn học phiêu lưu Pháp.",
                    false, "classic"),
            new BookSeed("Bá Tước Monte Cristo", "Alexandre Dumas", "phieu_luu", "ba_tuoc.webp",
                    149000, 129000, 24, 1844,
                    "Hành trình trả thù đầy mưu lược của một người đàn ông bị hãm hại oan uổng. Edmond Dantès, một thủy thủ trẻ tài giỏi, bị những kẻ ghen ghét vu oan và giam cầm oan uổng suốt mười bốn năm trong ngục tối. Sau khi vượt ngục và tìm được kho báu bí mật, anh trở thành Bá tước Monte Cristo giàu có để thực hiện kế hoạch trả thù tỉ mỉ những kẻ đã hãm hại mình. Tác phẩm là bản anh hùng ca về công lý, số phận và câu hỏi liệu sự trả thù có thực sự mang lại bình yên.",
                    true, "classic", "bestseller"),
            new BookSeed("Hai Vạn Dặm Dưới Đáy Biển", "Jules Verne", "phieu_luu", "day_bien.jpg",
                    89000, 89000, 30, 1870,
                    "Cuộc phiêu lưu kỳ thú dưới đại dương cùng thuyền trưởng Nemo và tàu ngầm Nautilus. Giáo sư Aronnax cùng các cộng sự bị bắt giữ trên con tàu ngầm bí ẩn Nautilus do thuyền trưởng Nemo điều khiển. Từ đó, họ được chứng kiến những kỳ quan choáng ngợp dưới lòng đại dương, từ rặng san hô rực rỡ đến những sinh vật biển khổng lồ chưa từng được biết đến. Tác phẩm thể hiện trí tưởng tượng vượt thời đại của Jules Verne về công nghệ và thế giới dưới nước.",
                    false, "classic"),
            new BookSeed("Kẻ Trộm Sách", "Markus Zusak", "phieu_luu", "trom_sach.jpg",
                    119000, 99000, 33, 2005,
                    "Câu chuyện cảm động về một cô bé mê sách giữa nước Đức thời Thế chiến II. Liesel Meminger, một cô bé nuôi tại nước Đức Quốc xã, tìm thấy niềm an ủi trong những cuốn sách mà em mượn từ nhiều nơi khác nhau. Giữa bối cảnh chiến tranh tàn khốc, tình cảm gia đình nuôi và tình bạn với cậu bé Do Thái trốn trong hầm nhà trở thành nguồn sáng hiếm hoi. Câu chuyện được kể qua góc nhìn độc đáo của Thần Chết, mang đến một góc nhìn đầy ám ảnh về chiến tranh và sức mạnh của ngôn từ.",
                    false, "bestseller"),
            new BookSeed("Robinson Crusoe", "Daniel Defoe", "phieu_luu", "robinson.jpg",
                    75000, 75000, 26, 1719,
                    "Hành trình sinh tồn một mình trên hoang đảo suốt nhiều năm của một thủy thủ gặp nạn. Sau một trận đắm tàu, Robinson Crusoe trôi dạt vào một hòn đảo hoang không một bóng người và buộc phải tự tay xây dựng lại cuộc sống từ đầu. Từ việc trồng trọt, săn bắn đến chế tạo công cụ, anh dần biến hòn đảo hoang thành nơi trú ngụ có thể sinh tồn lâu dài. Cuộc gặp gỡ với người bản địa Thứ Sáu sau đó mở ra một chương mới đầy tình người trong hành trình cô độc của anh.",
                    false, "classic"),
            new BookSeed("Tiếng Gọi Nơi Hoang Dã", "Jack London", "phieu_luu", "hoang_da.jpg",
                    69000, 69000, 28, 1903,
                    "Câu chuyện về chú chó Buck trở về với bản năng hoang dã giữa vùng đất Alaska khắc nghiệt. Từ một chú chó nhà được nuông chiều, Buck bị bắt cóc và bán làm chó kéo xe trượt tuyết giữa vùng đất giá lạnh Klondike thời kỳ đổ xô tìm vàng. Qua những trải nghiệm khắc nghiệt và tàn bạo, bản năng hoang dã tiềm ẩn trong Buck dần trỗi dậy mạnh mẽ. Tác phẩm là lời suy ngẫm sâu sắc về ranh giới mong manh giữa văn minh và bản năng nguyên thủy.",
                    false, "classic"),
            new BookSeed("Ánh Sáng Vô Hình", "Anthony Doerr", "phieu_luu", "anh_sang.webp",
                    139000, 119000, 20, 2014,
                    "Câu chuyện đan xen số phận của một cô gái mù người Pháp và một cậu bé lính Đức trong Thế chiến II. Marie-Laure, một cô gái Pháp bị mù từ nhỏ, mang theo bí mật về một viên đá quý trong khi chạy trốn quân Đức chiếm đóng. Cùng lúc đó, Werner, một cậu bé Đức tài năng về vô tuyến điện, bị cuốn vào bộ máy chiến tranh của Đức Quốc xã trái với ý muốn. Số phận hai con người dần giao thoa tại thành phố Saint-Malo, tạo nên một câu chuyện đầy chất thơ về ánh sáng giữa bóng tối chiến tranh.",
                    false, "bestseller"),
            new BookSeed("Đảo Giấu Vàng", "Robert Louis Stevenson", "phieu_luu", "dao_vang.webp",
                    79000, 79000, 32, 1883,
                    "Cuộc phiêu lưu tìm kho báu hải tặc đầy kịch tính của cậu bé Jim Hawkins. Sau khi tình cờ có được tấm bản đồ kho báu của một tên cướp biển đã chết, Jim Hawkins cùng đoàn thủy thủ lên đường tìm đến hòn đảo bí ẩn. Trên tàu, tên đầu bếp một chân Long John Silver ẩn giấu một kế hoạch phản trắc khiến hành trình trở nên nguy hiểm khôn lường. Tác phẩm được xem là một trong những cuốn tiểu thuyết phiêu lưu kinh điển định hình hình tượng cướp biển trong văn hóa đại chúng.",
                    false, "classic"),

            // ===== Thiếu nhi =====
            new BookSeed("Alice Ở Xứ Sở Diệu Kỳ", "Lewis Carroll", "thieu_nhi", "alice.jpg",
                    79000, 79000, 40, 1865,
                    "Cuộc phiêu lưu kỳ ảo của cô bé Alice tại vùng đất kỳ lạ sau khi rơi xuống hang thỏ. Trong lúc mơ màng bên bờ sông, Alice bất ngờ đuổi theo một chú thỏ trắng biết nói và rơi tọt xuống một cái hang dẫn vào thế giới kỳ ảo. Tại đó, cô gặp gỡ hàng loạt nhân vật kỳ quặc như Mèo Cheshire, Thỏ Tháng Ba và Nữ hoàng Cơ độc đoán. Tác phẩm nổi tiếng với lối viết giàu tưởng tượng, chơi chữ tinh tế, trở thành một trong những tác phẩm văn học thiếu nhi có ảnh hưởng nhất mọi thời đại.",
                    false, "classic"),
            new BookSeed("Chú Bé Mang Pyjama Sọc", "John Boyne", "thieu_nhi", "pyjama.jpg",
                    89000, 79000, 30, 2006,
                    "Câu chuyện cảm động và đau lòng về tình bạn vượt qua ranh giới trại tập trung thời Thế chiến II. Bruno, con trai một sĩ quan Đức Quốc xã, chuyển đến sống gần một trại tập trung mà cậu không hề hiểu rõ bản chất. Qua hàng rào dây thép gai, cậu kết bạn với Shmuel, một cậu bé Do Thái cùng tuổi bị giam giữ trong trại. Tình bạn ngây thơ giữa hai đứa trẻ dần dẫn đến một kết cục bi thảm, khiến người đọc phải suy ngẫm về sự vô tội bị cuốn vào tội ác của người lớn.",
                    true, "bestseller"),
            new BookSeed("Cây Cam Ngọt Của Tôi", "José Mauro de Vasconcelos", "thieu_nhi", "cay_cam.jpg",
                    89000, 89000, 35, 1968,
                    "Tuổi thơ hồn nhiên nhưng đầy nước mắt của cậu bé Zezé nghèo khó ở Brazil. Zezé, cậu bé năm tuổi lớn lên trong một gia đình nghèo khó ở Brazil, thường xuyên bị đòn roi nhưng vẫn giữ trong lòng trí tưởng tượng phong phú. Cậu trò chuyện với cây cam nhỏ trong vườn như một người bạn tri kỷ và tìm thấy tình thương hiếm hoi nơi người hàng xóm tốt bụng Bồ Đào Nha. Câu chuyện dịu dàng nhưng đầy nước mắt này chạm đến trái tim của biết bao thế hệ độc giả về tuổi thơ và mất mát.",
                    false, "bestseller"),
            new BookSeed("Hoàng Tử Bé", "Antoine de Saint-Exupéry", "thieu_nhi", "hoang_tu_be.webp",
                    69000, 65000, 50, 1943,
                    "Câu chuyện triết lý nhẹ nhàng về tình yêu thương, sự cô đơn qua lời kể của một hoàng tử đến từ hành tinh khác. Sau khi máy bay gặp sự cố buộc phải hạ cánh giữa sa mạc Sahara, phi công gặp một cậu bé kỳ lạ tự xưng là hoàng tử đến từ một hành tinh nhỏ bé. Qua những cuộc trò chuyện, hoàng tử kể lại hành trình ghé thăm nhiều hành tinh khác nhau và những bài học về tình yêu, trách nhiệm mà cậu học được từ đóa hồng của mình. Tác phẩm mang đến những triết lý giản dị nhưng sâu sắc về cách nhìn thế giới bằng trái tim thay vì chỉ bằng lý trí.",
                    true, "bestseller", "classic"),
            new BookSeed("Mạng Nhện Của Charlotte", "E.B. White", "thieu_nhi", "mang_nhen.jpg",
                    75000, 75000, 27, 1952,
                    "Tình bạn cảm động giữa chú heo Wilbur và nhện Charlotte trong trang trại. Wilbur, chú heo con nhỏ bé nhất đàn, đứng trước nguy cơ bị giết thịt cho đến khi được nhện Charlotte ở góc chuồng ra tay giúp đỡ. Bằng cách dệt những dòng chữ ca ngợi trên mạng nhện, Charlotte khiến Wilbur trở nên nổi tiếng và được cứu sống. Câu chuyện giản dị về tình bạn, sự hy sinh và vòng tuần hoàn của sự sống đã trở thành một trong những tác phẩm thiếu nhi được yêu thích nhất mọi thời đại.",
                    false, "classic"),
            new BookSeed("Phù Thủy Xứ Oz", "L. Frank Baum", "thieu_nhi", "witch_oz.webp",
                    79000, 79000, 24, 1900,
                    "Hành trình kỳ diệu của cô bé Dorothy cùng những người bạn trên con đường gạch vàng. Sau khi bị cơn lốc xoáy cuốn khỏi Kansas, Dorothy cùng chú chó Toto lạc vào xứ Oz đầy màu sắc và phép thuật. Trên con đường gạch vàng tìm về nhà, cô kết bạn với Bù Nhìn Rơm mong có bộ óc, Người Thiếc mong có trái tim và Sư Tử Nhát Gan mong có lòng dũng cảm. Cùng nhau, họ đối mặt với Phù Thủy Độc Ác Phương Tây để tìm gặp Phù Thủy vĩ đại xứ Oz.",
                    false, "classic"),
            new BookSeed("Pippi Tất Dài", "Astrid Lindgren", "thieu_nhi", "pippi.webp",
                    75000, 75000, 26, 1945,
                    "Những trò nghịch ngợm đáng yêu của cô bé Pippi mạnh mẽ, độc lập và giàu trí tưởng tượng. Sống một mình trong căn biệt thự kỳ lạ cùng chú khỉ và con ngựa của mình, Pippi sở hữu sức mạnh phi thường và không cần tuân theo bất kỳ quy tắc nào của người lớn. Cô cùng hai người bạn hàng xóm Tommy và Annika trải qua vô số cuộc phiêu lưu bất ngờ và hài hước ngay trong thị trấn nhỏ của mình. Nhân vật Pippi đã trở thành biểu tượng cho tinh thần tự do và phá cách trong văn học thiếu nhi Thụy Điển.",
                    false, "classic"),
            new BookSeed("Totto-chan Bên Cửa Sổ", "Tetsuko Kuroyanagi", "thieu_nhi", "totto.webp",
                    89000, 79000, 38, 1981,
                    "Hồi ức tuổi thơ ấm áp về ngôi trường đặc biệt đã nuôi dưỡng một cô bé cá tính. Vì quá nghịch ngợm và khác biệt, Totto-chan bị đuổi khỏi trường tiểu học đầu tiên và được mẹ gửi đến ngôi trường Tomoe đặc biệt với những toa tàu cũ làm lớp học. Tại đó, thầy hiệu trưởng Kobayashi luôn kiên nhẫn lắng nghe và tôn trọng sự khác biệt của từng học sinh. Cuốn hồi ký dựa trên câu chuyện có thật này đã truyền cảm hứng cho hàng triệu độc giả về một nền giáo dục nhân văn và thấu hiểu trẻ em.",
                    false, "bestseller"),

            // ===== Trinh thám =====
            new BookSeed("Cô Gái Có Hình Xăm Rồng", "Stieg Larsson", "trinh_tham", "co_gai_co_hinh_xam_rong.jpg",
                    129000, 109000, 22, 2005,
                    "Vụ án bí ẩn kéo dài hàng chục năm được phá giải bởi nhà báo và một nữ hacker kỳ lạ. Nhà báo Mikael Blomkvist được thuê điều tra vụ mất tích bí ẩn của một cô gái trẻ xảy ra từ nhiều thập kỷ trước trong một gia tộc giàu có. Anh bắt tay cùng Lisbeth Salander, một nữ hacker thiên tài mang quá khứ đầy tổn thương, để lật lại từng manh mối bị chôn giấu. Cuộc điều tra dần hé lộ những bí mật đen tối và tội ác kinh hoàng ẩn sau vẻ ngoài hào nhoáng của gia tộc quyền lực.",
                    false, "bestseller"),
            new BookSeed("Cô Gái Mất Tích", "Gillian Flynn", "trinh_tham", "co_gai_mat_tich.webp",
                    119000, 99000, 30, 2012,
                    "Cuộc hôn nhân tưởng chừng hoàn hảo bất ngờ trở thành một vụ án đầy twist khó lường. Vào đúng ngày kỷ niệm cưới, Amy Dunne bỗng dưng biến mất một cách bí ẩn, khiến chồng cô, Nick, trở thành nghi phạm số một trong mắt cảnh sát và dư luận. Qua những trang nhật ký xen kẽ góc nhìn của cả hai vợ chồng, sự thật về cuộc hôn nhân dần được hé lộ theo những cách không ai ngờ tới. Tác phẩm nổi tiếng với cú twist gây sốc, phơi bày mặt tối của hôn nhân và truyền thông hiện đại.",
                    true, "bestseller"),
            new BookSeed("Mật Mã Da Vinci", "Dan Brown", "trinh_tham", "mat_ma_davinci.jpg",
                    139000, 119000, 40, 2003,
                    "Cuộc truy tìm bí ẩn tôn giáo ẩn giấu trong các tác phẩm nghệ thuật của Leonardo da Vinci. Sau vụ án mạng bí ẩn tại Bảo tàng Louvre, nhà biểu tượng học Robert Langdon bị cuốn vào một chuỗi manh mối được mã hóa trong các tác phẩm của Leonardo da Vinci. Cùng nhà mật mã học Sophie Neveu, anh phải giải mã từng lớp bí ẩn để ngăn chặn một âm mưu che giấu sự thật tôn giáo chấn động suốt hàng thế kỷ. Tác phẩm gây tranh cãi lớn khi ra mắt nhưng cũng trở thành hiện tượng xuất bản toàn cầu.",
                    true, "bestseller"),
            new BookSeed("Phía Sau Nghi Can X", "Keigo Higashino", "trinh_tham", "phia_sau_nghi_can_x.webp",
                    99000, 89000, 33, 2005,
                    "Vụ án giết người được che giấu bởi kế hoạch hoàn hảo của một thiên tài toán học. Khi một người phụ nữ vô tình giết chết người chồng vũ phu cũ, người hàng xóm là một thiên tài toán học đã ra tay giúp cô che giấu tội ác bằng một kế hoạch tưởng như hoàn hảo. Cuộc đấu trí căng thẳng diễn ra giữa anh và một người bạn cũ làm việc trong ngành điều tra hình sự. Tác phẩm được đánh giá cao vì cách xây dựng vụ án tinh vi, nơi độc giả biết trước hung thủ nhưng vẫn bị cuốn theo từng bước phá án.",
                    false, "bestseller"),
            new BookSeed("Sherlock Holmes", "Arthur Conan Doyle", "trinh_tham", "sherlock_home.webp",
                    109000, 109000, 45, 1887,
                    "Những vụ án ly kỳ được phá giải bằng lý luận sắc bén của thám tử lừng danh Sherlock Holmes. Cùng người bạn đồng hành trung thành, bác sĩ Watson, thám tử Sherlock Holmes sử dụng khả năng quan sát tỉ mỉ và suy luận logic để phá giải những vụ án tưởng chừng không có lời giải tại London thế kỷ 19. Từ những vụ trộm cắp tinh vi đến các âm mưu giết người phức tạp, mỗi câu chuyện đều là một màn trình diễn trí tuệ đỉnh cao. Nhân vật Sherlock Holmes đã trở thành hình mẫu kinh điển cho thể loại trinh thám suốt hơn một thế kỷ qua.",
                    false, "classic"),
            new BookSeed("Sự Im Lặng Của Bầy Cừu", "Thomas Harris", "trinh_tham", "su_im_lang_cua_bay_cuu.webp",
                    99000, 99000, 20, 1988,
                    "Cuộc đối đầu tâm lý căng thẳng giữa nữ đặc vụ FBI và kẻ sát nhân hàng loạt thiên tài. Nữ đặc vụ FBI trẻ tuổi Clarice Starling được giao nhiệm vụ thẩm vấn Hannibal Lecter, một bác sĩ tâm thần thiên tài nhưng cũng là kẻ sát nhân hàng loạt nguy hiểm, để tìm manh mối về một tên tội phạm khác đang lẩn trốn. Qua từng cuộc trò chuyện đầy ám ảnh, ranh giới giữa việc lợi dụng và bị thao túng tâm lý trở nên mong manh. Tác phẩm khắc họa sâu sắc cuộc đấu tranh nội tâm và sự can đảm của Clarice trước cái ác tinh vi.",
                    false, "classic"),
            new BookSeed("Và Rồi Chẳng Còn Ai", "Agatha Christie", "trinh_tham", "va_roi_chang_con_ai.webp",
                    89000, 79000, 36, 1939,
                    "Mười người xa lạ bị mắc kẹt trên hòn đảo và lần lượt bị sát hại theo một bài đồng dao bí ẩn. Mười người xa lạ được mời đến một hòn đảo biệt lập ngoài khơi nước Anh với những lý do khác nhau, nhưng không ai trong số họ biết chủ nhân thực sự là ai. Lần lượt từng người bị sát hại theo đúng trình tự của bài đồng dao, khiến nỗi sợ hãi và nghi kỵ lẫn nhau ngày càng leo thang. Đây được xem là một trong những tiểu thuyết trinh thám bán chạy nhất mọi thời đại nhờ cấu trúc bí ẩn khép kín đầy sáng tạo.",
                    true, "bestseller", "classic"),
            new BookSeed("Án Mạng Trên Chuyến Tàu Tốc Hành Phương Đông", "Agatha Christie", "trinh_tham", "an_mang_tren_chuyen_tau.jpg",
                    89000, 79000, 30, 1934,
                    "Vụ án mạng bí ẩn trên chuyến tàu bị mắc kẹt giữa tuyết được thám tử Poirot phá giải. Khi chuyến tàu tốc hành sang trọng bị mắc kẹt giữa những đống tuyết dày đặc, một hành khách bí ẩn bị phát hiện đã chết trong khoang riêng với nhiều vết dao đâm. Thám tử Hercule Poirot, cũng đang có mặt trên tàu, phải thẩm vấn từng hành khách để tìm ra sự thật giữa vô số lời khai mâu thuẫn. Cái kết bất ngờ của vụ án đã trở thành một trong những twist nổi tiếng nhất trong lịch sử thể loại trinh thám.",
                    false, "classic"),

            // ===== Tình cảm =====
            new BookSeed("Gọi Em Bằng Tên Anh", "André Aciman", "tinh_cam", "goi_em_bang_ten_anh.jpg",
                    109000, 95000, 25, 2007,
                    "Câu chuyện tình yêu mùa hè đầy cảm xúc giữa hai chàng trai trẻ ở miền quê nước Ý. Trong một mùa hè yên bình tại miền quê Ý, Elio, chàng trai mười bảy tuổi, dần nảy sinh tình cảm đặc biệt với Oliver, vị khách trẻ tuổi đến ở cùng gia đình anh. Giữa những buổi chiều đọc sách, đạp xe và bơi lội, tình cảm giữa hai người lớn dần trong sự do dự và khát khao thầm kín. Tác phẩm được ca ngợi vì lối viết tinh tế, giàu cảm xúc khi khắc họa tình yêu đầu đời và những rung động tuổi trẻ.",
                    false, "new"),
            new BookSeed("Jane Eyre", "Charlotte Brontë", "tinh_cam", "jane_eyre.jpg",
                    99000, 99000, 30, 1847,
                    "Hành trình trưởng thành và tìm kiếm tình yêu tự do của một cô gia sư giàu nghị lực. Mồ côi từ nhỏ và lớn lên trong sự ghẻ lạnh, Jane Eyre trở thành gia sư tại điền trang Thornfield, nơi cô gặp gỡ và đem lòng yêu ông chủ bí ẩn Edward Rochester. Tuy nhiên, một bí mật đen tối bị giấu kín trong căn gác mái đe dọa phá vỡ hạnh phúc mà cô vừa tìm được. Tác phẩm được xem là tiếng nói tiên phong cho quyền bình đẳng và độc lập của phụ nữ trong văn học Anh thế kỷ 19.",
                    false, "classic"),
            new BookSeed("Kiêu Hãnh Và Định Kiến", "Jane Austen", "tinh_cam", "kieu_hanh_va_dinh_kien.webp",
                    99000, 89000, 42, 1813,
                    "Câu chuyện tình yêu kinh điển vượt qua định kiến giai cấp trong xã hội Anh thế kỷ 19. Elizabeth Bennet, cô gái thông minh và sắc sảo, ban đầu có ấn tượng không mấy tốt đẹp với ngài Darcy kiêu ngạo và giàu có. Qua nhiều lần chạm mặt và hiểu lầm, cả hai dần nhận ra những định kiến ban đầu của mình đã che khuất sự thật về đối phương. Tác phẩm của Jane Austen nổi tiếng với lối văn châm biếm tinh tế và cái nhìn sắc sảo về hôn nhân, giai cấp trong xã hội Anh đương thời.",
                    true, "bestseller", "classic"),
            new BookSeed("Nhật Ký Tình Yêu", "Nicholas Sparks", "tinh_cam", "nhat_ky_tinh_yeu.webp",
                    89000, 79000, 34, 1996,
                    "Câu chuyện tình yêu bền bỉ vượt thời gian được ghi lại qua cuốn nhật ký cũ. Mỗi ngày, một người đàn ông lớn tuổi đọc lại cho vợ mình nghe câu chuyện tình yêu từ cuốn nhật ký cũ, dù bà đã không còn nhớ được ai bên cạnh mình. Câu chuyện trong nhật ký kể về mối tình đầy sóng gió giữa hai con người thuộc hai tầng lớp khác biệt bị chia cắt bởi hoàn cảnh gia đình. Tác phẩm chạm đến trái tim độc giả với thông điệp về tình yêu bền vững vượt qua mọi thử thách của thời gian và bệnh tật.",
                    false, "bestseller"),
            new BookSeed("Nếu Em Không Phải Một Giấc Mơ", "Marc Levy", "tinh_cam", "neu_em_khong_phai_mot_giac_mo.jpg",
                    85000, 85000, 22, 2000,
                    "Câu chuyện tình yêu kỳ lạ và cảm động giữa một chàng trai và một cô gái tưởng như không có thật. Arthur, một kiến trúc sư sống một mình, bất ngờ gặp Lauren, một cô gái xuất hiện đầy bí ẩn ngay trong căn hộ của anh sau một tai nạn kỳ lạ. Chỉ có anh mới có thể nhìn thấy và chạm vào cô, trong khi với những người khác, Lauren dường như không hề tồn tại. Câu chuyện tình yêu vượt qua ranh giới thực và ảo này dần hé lộ bí mật đằng sau sự xuất hiện kỳ lạ của cô gái.",
                    false, "new"),
            new BookSeed("Rừng Na Uy", "Haruki Murakami", "tinh_cam", "rung_nauy.webp",
                    109000, 95000, 28, 1987,
                    "Câu chuyện tình yêu, mất mát và trưởng thành của chàng sinh viên Toru giữa Tokyo thập niên 1960. Toru Watanabe, chàng sinh viên đại học tại Tokyo, chìm trong nỗi hoài niệm và mất mát sau cái chết của người bạn thân thời trung học. Anh dần bị giằng xé giữa tình cảm dành cho Naoko, người bạn gái mong manh mang nhiều tổn thương, và Midori, cô gái tràn đầy sức sống. Tác phẩm của Haruki Murakami khắc họa tinh tế nỗi cô đơn, tình dục và sự trưởng thành của tuổi trẻ Nhật Bản thời kỳ biến động.",
                    true, "bestseller"),
            new BookSeed("Trước Ngày Em Đến", "Jojo Moyes", "tinh_cam", "truoc_ngay_em_den.jpg",
                    99000, 85000, 32, 2012,
                    "Câu chuyện cảm động về tình yêu và những lựa chọn khó khăn trong cuộc sống. Louisa Clark, một cô gái trẻ vừa mất việc, nhận công việc chăm sóc Will Traynor, một người đàn ông từng tràn đầy sức sống nhưng nay bị liệt tứ chi sau tai nạn. Ban đầu xa cách và khó chịu, Will dần mở lòng và cả hai bắt đầu nảy sinh tình cảm sâu sắc dành cho nhau. Câu chuyện đặt ra những câu hỏi day dứt về ý nghĩa cuộc sống, quyền được lựa chọn và giới hạn của tình yêu.",
                    false, "bestseller"),
            new BookSeed("Đồi Gió Hú", "Emily Brontë", "tinh_cam", "doi_gio_hu.webp",
                    99000, 99000, 24, 1847,
                    "Câu chuyện tình yêu ám ảnh và đầy thù hận trên vùng đồng hoang nước Anh. Heathcliff, một đứa trẻ mồ côi được nhận nuôi, lớn lên cùng tình yêu mãnh liệt dành cho Catherine Earnshaw nhưng bị ngăn cản bởi khoảng cách giai cấp. Khi Catherine chọn kết hôn với người khác vì địa vị xã hội, Heathcliff chìm trong nỗi hận thù và tìm cách trả thù cả hai gia đình qua nhiều thế hệ. Tác phẩm nổi tiếng với bầu không khí u ám, dữ dội trên vùng đồng hoang nước Anh, phản ánh mặt tối và đam mê cực đoan của tình yêu.",
                    false, "classic"),

            // ===== Văn học kinh điển =====
            new BookSeed("Bắt Trẻ Đồng Xanh", "J.D. Salinger", "van_hoc_kinh_dien", "bat_tre_dong_xanh.jpg",
                    89000, 89000, 26, 1951,
                    "Những suy nghĩ nổi loạn và lạc lõng của một thiếu niên trong hành trình tìm kiếm bản thân. Sau khi bị đuổi học lần thứ tư, Holden Caulfield lang thang khắp thành phố New York suốt vài ngày trước khi trở về nhà đối diện với gia đình. Qua những cuộc gặp gỡ tình cờ, cậu bộc lộ sự chán ghét sâu sắc với sự giả tạo của thế giới người lớn xung quanh mình. Tác phẩm trở thành tiếng nói tiêu biểu cho sự nổi loạn và khủng hoảng bản sắc của thanh thiếu niên thế kỷ 20.",
                    false, "classic"),
            new BookSeed("Chiến Tranh Và Hòa Bình", "Lev Tolstoy", "van_hoc_kinh_dien", "chien_tranh_va_hoa_binh.jpg",
                    199000, 179000, 15, 1869,
                    "Bức tranh sử thi đồ sộ về xã hội Nga trong thời kỳ chiến tranh Napoleon. Tác phẩm theo chân nhiều gia tộc quý tộc Nga, đặc biệt là các nhân vật Pierre Bezukhov, Andrei Bolkonsky và Natasha Rostova, qua những biến động của xã hội Nga trong thời kỳ chiến tranh với Napoleon. Giữa khói lửa chiến trận và những buổi dạ hội xa hoa, mỗi nhân vật đều phải tìm kiếm ý nghĩa cuộc sống của riêng mình. Được xem là một trong những kiệt tác vĩ đại nhất của văn học thế giới, tác phẩm kết hợp giữa lịch sử, triết học và tâm lý con người một cách sâu sắc.",
                    false, "classic"),
            new BookSeed("Cuốn Theo Chiều Gió", "Margaret Mitchell", "van_hoc_kinh_dien", "cuon_theo_chieu_gio.webp",
                    159000, 139000, 20, 1936,
                    "Câu chuyện tình yêu và nghị lực sống của Scarlett giữa cuộc Nội chiến Hoa Kỳ. Scarlett O'Hara, tiểu thư kiêu kỳ của một đồn điền miền Nam nước Mỹ, phải học cách sinh tồn khi cuộc Nội chiến tàn phá toàn bộ cuộc sống xa hoa mà cô từng biết đến. Giữa những mất mát và đổ vỡ, mối tình đầy trắc trở giữa cô và chàng Rhett Butler phóng khoáng trở thành sợi dây xuyên suốt câu chuyện. Tác phẩm khắc họa sống động sự sụp đổ của một thời đại và nghị lực phi thường của con người trước nghịch cảnh.",
                    true, "bestseller", "classic"),
            new BookSeed("Giết Con Chim Nhại", "Harper Lee", "van_hoc_kinh_dien", "giet_con_chim_nhai.webp",
                    99000, 89000, 30, 1960,
                    "Câu chuyện về công lý và định kiến chủng tộc qua góc nhìn của một cô bé miền Nam nước Mỹ. Qua lời kể của cô bé Scout Finch, câu chuyện tái hiện lại phiên tòa xét xử oan sai một người đàn ông da đen bị buộc tội hãm hiếp tại một thị trấn nhỏ miền Nam nước Mỹ những năm 1930. Cha của Scout, luật sư Atticus Finch, kiên quyết bảo vệ công lý dù phải đối mặt với sự kỳ thị và thù ghét từ chính cộng đồng mình. Tác phẩm trở thành biểu tượng văn học về lòng chính trực, sự cảm thông và cuộc đấu tranh chống lại định kiến chủng tộc.",
                    true, "bestseller", "classic"),
            new BookSeed("Hai Số Phận", "Jeffrey Archer", "van_hoc_kinh_dien", "hai_so_phan.jpg",
                    139000, 119000, 25, 1979,
                    "Câu chuyện về hai con người khác biệt hoàn cảnh nhưng có chung ngày sinh, đan xen số phận qua nhiều thập kỷ. Sinh ra cùng ngày nhưng ở hai thái cực đối lập, một người lớn lên trong cảnh nghèo khó còn người kia thừa hưởng sự giàu sang từ gia tộc quyền quý. Số phận hai người dần đan xen qua tình bạn, tình yêu và những bí mật gia đình được hé lộ dần theo năm tháng. Bộ tiểu thuyết nhiều tập này cuốn hút người đọc bởi những nút thắt bất ngờ trải dài qua nhiều thập kỷ của lịch sử nước Anh.",
                    false, "bestseller"),
            new BookSeed("Không Gia Đình", "Hector Malot", "van_hoc_kinh_dien", "khong_gia_dinh.webp",
                    99000, 99000, 28, 1878,
                    "Hành trình lưu lạc mưu sinh đầy nước mắt của cậu bé mồ côi Rémi. Rémi, cậu bé bị bỏ rơi từ nhỏ, được một người đàn ông tốt bụng tên Vitalis nhận nuôi và cùng đoàn xiếc rong ruổi khắp nước Pháp để mưu sinh. Trên hành trình gian khổ ấy, cậu trải qua đói rét, mất mát người thân yêu và không ngừng tìm kiếm cội nguồn gia đình thật sự của mình. Tác phẩm là câu chuyện cảm động về nghị lực sống, tình người và khát vọng được thuộc về một nơi chốn gọi là nhà.",
                    false, "classic"),
            new BookSeed("Những Người Khốn Khổ", "Victor Hugo", "van_hoc_kinh_dien", "nhung_nguoi_khon_kho.webp",
                    179000, 159000, 18, 1862,
                    "Câu chuyện cứu rỗi và công lý xoay quanh cuộc đời cựu tù nhân Jean Valjean. Sau mười chín năm tù vì ăn cắp một ổ bánh mì, Jean Valjean được một vị giám mục nhân từ cảm hóa và quyết tâm sống lương thiện, dù luôn bị viên thanh tra Javert truy đuổi không ngừng. Trên hành trình chuộc lỗi, ông nhận nuôi Cosette, con gái của một người phụ nữ bất hạnh, và dành trọn tình yêu thương cho cô như con ruột. Tác phẩm đồ sộ của Victor Hugo là bản anh hùng ca về lòng vị tha, công lý và khát vọng đổi thay xã hội Pháp thế kỷ 19.",
                    true, "bestseller", "classic"),
            new BookSeed("Trăm Năm Cô Đơn", "Gabriel Garcia Marquez", "van_hoc_kinh_dien", "tram_nam_co_don.webp",
                    139000, 119000, 20, 1967,
                    "Câu chuyện huyền ảo về nhiều thế hệ dòng họ Buendía tại thị trấn Macondo. Tác phẩm kể lại bảy thế hệ của dòng họ Buendía tại thị trấn hư cấu Macondo, nơi thực tại và huyền ảo hòa quyện vào nhau một cách kỳ lạ. Những vòng lặp định mệnh, tình yêu cấm kỵ và nỗi cô đơn truyền kiếp ám ảnh từng thành viên trong gia tộc qua nhiều thế hệ. Được xem là kiệt tác tiêu biểu của trường phái hiện thực huyền ảo, tác phẩm đã mang về cho Gabriel García Márquez giải Nobel Văn học.",
                    true, "bestseller", "classic"),
            new BookSeed("Ông Già Và Biển Cả", "Ernest Hemingway", "van_hoc_kinh_dien", "ong_gia_va_bien_ca.webp",
                    69000, 65000, 40, 1952,
                    "Cuộc chiến đơn độc và kiên cường của một ngư ông già với con cá kiếm khổng lồ. Sau 84 ngày ra khơi không bắt được con cá nào, lão ngư phủ quyết tâm ra biển một mình lần nữa và đối đầu với một con cá kiếm khổng lồ ngoài khơi xa. Cuộc chiến kéo dài nhiều ngày đêm giữa lão và con cá trở thành biểu tượng cho ý chí không khuất phục của con người trước thiên nhiên. Dù cuối cùng con cá bị đàn cá mập rỉa sạch, chiến thắng tinh thần của lão ngư phủ vẫn toả sáng, khẳng định rằng con người có thể bị hủy diệt nhưng không thể bị đánh bại.",
                    false, "classic")
    );

    // ============================================================
    // 3. GÁN NHÀ XUẤT BẢN THEO CATEGORY (dữ liệu mẫu, không có sẵn NXB thật)
    // ============================================================
    private static final Map<String, String> PUBLISHER_BY_CATEGORY = new HashMap<>();
    static {
        PUBLISHER_BY_CATEGORY.put("khoa_hoc_vien_tuong", "Nhã Nam");
        PUBLISHER_BY_CATEGORY.put("phieu_luu", "NXB Kim Đồng");
        PUBLISHER_BY_CATEGORY.put("thieu_nhi", "NXB Kim Đồng");
        PUBLISHER_BY_CATEGORY.put("trinh_tham", "NXB Trẻ");
        PUBLISHER_BY_CATEGORY.put("tinh_cam", "Nhã Nam");
        PUBLISHER_BY_CATEGORY.put("van_hoc_kinh_dien", "NXB Văn Học");
    }

    // ============================================================
    // 4. HÀM CHÍNH - GỌI HÀM NÀY DUY NHẤT
    // ============================================================
    public static void seedFullCatalog(FirebaseCallback<Void> callback) {
        FirebaseFirestore db = FirebaseUtils.getFirestore();
        WriteBatch batch = db.batch();

        CollectionReference categoriesRef = db.collection("categories");
        CollectionReference authorsRef = db.collection("authors");
        CollectionReference publishersRef = db.collection("publishers");
        CollectionReference booksRef = db.collection("books");

        // ---- 4.1 Tạo categories, lưu lại DocumentReference theo slug ----
        Map<String, DocumentReference> categoryRefs = new HashMap<>();
        for (CategorySeed cat : CATEGORIES) {
            DocumentReference ref = categoriesRef.document();
            categoryRefs.put(cat.slug, ref);

            Map<String, Object> data = new HashMap<>();
            data.put("name", cat.name);
            data.put("imageUrl", IMG_BASE + "/" + cat.slug + "/" + cat.iconFile);
            data.put("isActive", true);
            batch.set(ref, data);
        }

        // ---- 4.2 Tạo publishers (4 NXB mẫu), lưu theo tên ----
        Map<String, DocumentReference> publisherRefs = new HashMap<>();
        for (String pubName : new String[]{"Nhã Nam", "NXB Trẻ", "NXB Kim Đồng", "NXB Văn Học"}) {
            DocumentReference ref = publishersRef.document();
            publisherRefs.put(pubName, ref);

            Map<String, Object> data = new HashMap<>();
            data.put("name", pubName);
            data.put("logoUrl", "");
            data.put("description", "Đơn vị xuất bản sách " + pubName + ".");
            batch.set(ref, data);
        }

        // ---- 4.3 Tạo authors (gộp trùng tên - Dumas, Agatha Christie chỉ tạo 1 lần) ----
        Map<String, DocumentReference> authorRefs = new HashMap<>();
        for (BookSeed book : BOOKS) {
            if (!authorRefs.containsKey(book.author)) {
                DocumentReference ref = authorsRef.document();
                authorRefs.put(book.author, ref);

                Map<String, Object> data = new HashMap<>();
                data.put("name", book.author);
                data.put("avatarUrl", "");
                data.put("bio", "");
                data.put("bookCount", 0); // cập nhật số lượng thật ở bước dưới
                batch.set(ref, data);
            }
        }

        // Đếm số sách mỗi tác giả để set bookCount đúng (ghi đè lại data ở trên)
        Map<String, Integer> bookCountPerAuthor = new HashMap<>();
        for (BookSeed book : BOOKS) {
            bookCountPerAuthor.merge(book.author, 1, Integer::sum);
        }
        for (Map.Entry<String, DocumentReference> entry : authorRefs.entrySet()) {
            batch.update(entry.getValue(), "bookCount", bookCountPerAuthor.get(entry.getKey()));
        }

        // ---- 4.4 Tạo 49 books, tham chiếu đúng category/author/publisher vừa tạo ----
        for (BookSeed book : BOOKS) {
            DocumentReference categoryRef = categoryRefs.get(book.categorySlug);
            DocumentReference authorRef = authorRefs.get(book.author);
            String publisherName = PUBLISHER_BY_CATEGORY.get(book.categorySlug);
            DocumentReference publisherRef = publisherRefs.get(publisherName);

            String imgUrl = IMG_BASE + "/" + book.categorySlug + "/"
                    + stripExtension(book.imgFile) + "/" + book.imgFile;

            Map<String, Object> data = new HashMap<>();
            data.put("title", book.title);
            data.put("slug", slugify(book.title));
            data.put("authorIds", Arrays.asList(authorRef.getId()));
            data.put("publisherId", publisherRef.getId());
            data.put("categoryIds", Arrays.asList(categoryRef.getId()));
            data.put("description", book.description);
            data.put("coverImageUrl", imgUrl);
            data.put("price", book.price);
            data.put("salePrice", book.salePrice);
            data.put("stock", book.stock);
            data.put("soldCount", 0);
            data.put("viewCount", 0);
            data.put("rating", 0);
            data.put("ratingCount", 0);
            data.put("publishYear", book.year);
            data.put("tags", book.tags);
            data.put("isFeatured", book.featured);
            data.put("isActive", true);

            batch.set(booksRef.document(), data);
        }

        // ---- 4.5 Commit tất cả (khoảng 106 thao tác: 6 category + 4 publisher + 47 author x2 + 49 book - dưới hạn mức 500 của 1 batch) ----
        batch.commit()
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    private static String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    /** Tạo slug đơn giản từ tên sách để lưu field "slug" (dùng cho search sau này). */
    private static String slugify(String text) {
        String normalized = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd').replace('Đ', 'D')
                .toLowerCase();
        return normalized.replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
    }
}