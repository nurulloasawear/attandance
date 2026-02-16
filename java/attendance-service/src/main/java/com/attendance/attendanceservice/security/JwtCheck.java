//package com.attendance.attendanceservice.security;
//
//public class JwtCheck {
//    public static void main(String[] args) {
//        String secret = "+i38C96CqmxwIheqUXRKPHpO/07bWCgLE2SnYJgOQl4=";
//
//        com.attendance.userservice.security.JwtService us =
//                new com.attendance.userservice.security.JwtService(secret, 600_000);
//
//        com.attendance.attendanceservice.security.JwtService as =
//                new com.attendance.attendanceservice.security.JwtService(secret);
//
//        String token = us.generateAccessToken(
//                "A1234567",
//                java.util.Map.of("role", "ADMIN"),
//                "sid-1"
//        );
//
//        System.out.println("token=" + token);
//        System.out.println("attendance isValid=" + as.isValid(token));
//        System.out.println("sub=" + as.extractSubject(token));
//        System.out.println("publicId=" + as.extractClaimString(token, "publicId"));
//    }
//}
//
