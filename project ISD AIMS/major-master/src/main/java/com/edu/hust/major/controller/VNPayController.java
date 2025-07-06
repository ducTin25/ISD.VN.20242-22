package com.edu.hust.major.controller;

import com.edu.hust.major.global.GlobalData;
import com.edu.hust.major.model.Product;
import com.edu.hust.major.service.ProductService;
import com.edu.hust.major.service.EmailService;
import com.edu.hust.major.repository.OrderRepository;
import com.edu.hust.major.model.Order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class VNPayController {
    
    @Value("${vnpay.tmnCode}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnp_HashSecret;

    @Value("${vnpay.payUrl}")
    private String vnp_PayUrl;

    @Value("${vnpay.returnUrl}")
    private String vnp_ReturnUrl;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping("/vnpay-payment")
    public String vnpayPayment(HttpServletRequest request, Model model) {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String address1 = request.getParameter("address1");
        String city = request.getParameter("city");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String note = request.getParameter("note");
        String shippingMethod = request.getParameter("shippingMethod");
        // ... kiểm tra các trường bắt buộc ...
        if (firstName == null || firstName.isEmpty() ||
            lastName == null || lastName.isEmpty() ||
            address1 == null || address1.isEmpty() ||
            city == null || city.isEmpty() ||
            phone == null || phone.isEmpty() ||
            email == null || email.isEmpty()) {
            model.addAttribute("message", "Vui lòng điền đầy đủ thông tin giao hàng!");
            return "checkout";
        }
        try {
            long amount = (long) (GlobalData.cart.stream().mapToDouble(Product::getPrice).sum() * 100);
            String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
            String vnp_IpAddr = "127.0.0.1"; // Lấy IP từ request, ví dụ: request.getRemoteAddr()

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + vnp_TxnRef);
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            for (String fieldName : fieldNames) {
                String fieldValue = vnp_Params.get(fieldName);
                hashData.append(fieldName).append("=").append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString())).append("&");
            }
            hashData.deleteCharAt(hashData.length() - 1);

            String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());

            StringBuilder queryUrl = new StringBuilder();
            for (String fieldName : fieldNames) {
                String fieldValue = vnp_Params.get(fieldName);
                queryUrl.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()))
                        .append("=")
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()))
                        .append("&");
            }
            queryUrl.deleteCharAt(queryUrl.length() - 1);
            queryUrl.append("&vnp_SecureHash=").append(vnp_SecureHash);

            String paymentUrl = vnp_PayUrl + "?" + queryUrl.toString();
            
            request.getSession().setAttribute("orderInfo", new Order(
                firstName, lastName, address1, "", city, zip, phone, email, note, shippingMethod, "Đang xử lý"
            ));
            
            return "redirect:" + paymentUrl;

        } catch (Exception e) {
            model.addAttribute("message", "Có lỗi khi tạo thanh toán VNPay: " + e.getMessage());
            return "checkout";
        }
    }

    @GetMapping("/vnpay-payment-return")
    public String vnpayReturn(HttpServletRequest request, Model model) {
        try {
            Map<String, String> fields = new HashMap<>();
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
                String paramName = params.nextElement();
                String paramValue = request.getParameter(paramName);
                fields.put(paramName, paramValue);
            }

            String vnp_SecureHash = fields.remove("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");

            List<String> fieldNames = new ArrayList<>(fields.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            for (String fieldName : fieldNames) {
                hashData.append(fieldName).append("=").append(URLEncoder.encode(fields.get(fieldName), StandardCharsets.US_ASCII.toString())).append("&");
            }
            hashData.deleteCharAt(hashData.length() - 1);

            String signValue = hmacSHA512(vnp_HashSecret, hashData.toString());

            if (signValue.equals(vnp_SecureHash)) {
                if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
                    // Lấy thông tin đơn hàng từ session
                    Order order = (Order) request.getSession().getAttribute("orderInfo");
                    if (order != null) {
                        order.setCreatedAt(new Date());
                        orderRepository.save(order);
                        request.getSession().removeAttribute("orderInfo");
                    }
                    GlobalData.cart.clear();
                    model.addAttribute("message", "Thanh toán thành công!");

                    // Lấy email từ request (nếu truyền qua form hoặc session)
                    String email = request.getParameter("email");
                    if (email != null && !email.isEmpty()) {
                        String subject = "Xác nhận đơn hàng thành công";
                        StringBuilder content = new StringBuilder();
                        content.append("Cảm ơn bạn đã đặt hàng!\n\n");
                        content.append("Chi tiết đơn hàng:\n");
                        // Thêm chi tiết sản phẩm
                        for (Product p : GlobalData.cart) {
                            content.append("- ").append(p.getName())
                                   .append(": ").append(p.getPrice()).append(" VND\n");
                        }
                        // Thêm tổng tiền
                        content.append("\nTổng tiền: ").append(request.getParameter("vnp_Amount")).append(" VND");
                        emailService.sendOrderDetails(email, subject, content.toString());
                    }
                } else {
                    model.addAttribute("message", "Thanh toán thất bại. Mã lỗi: " + request.getParameter("vnp_ResponseCode"));
                }
            } else {
                model.addAttribute("message", "Xác thực thất bại!");
            }
        } catch (Exception e) {
            model.addAttribute("message", "Lỗi trong xử lý kết quả thanh toán.");
        }
        model.addAttribute("cartCount", GlobalData.cart.size());
        return "checkout";
    }

    private String hmacSHA512(String key, String data) throws Exception {
        Mac hmac512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac512.init(secretKey);
        byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hash = new StringBuilder();
        for (byte b : bytes) {
            hash.append(String.format("%02x", b));
        }
        return hash.toString();
    }


}
