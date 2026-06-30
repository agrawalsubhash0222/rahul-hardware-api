package com.rahulhardware.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rahulhardware.dto.admin.AdminCustomerResponse;
import com.rahulhardware.entity.CustomerOrder;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByUserMobileOrderByCreatedAtDesc(String userMobile);

    List<CustomerOrder> findAllByOrderByCreatedAtDesc();

    Optional<CustomerOrder> findByIdAndUserMobile(Long id, String userMobile);

    @Query("""
                SELECT new com.rahulhardware.dto.admin.AdminCustomerResponse(
                    COALESCE(o.customerMobile, o.userMobile),
                    COALESCE(
                        MAX(o.customerName),
                        CONCAT(MAX(a.firstName), ' ', MAX(a.lastName)),
                        'Customer'
                    ),
                    MAX(a.email),
                    MAX(a.city),
                    MAX(a.state),
                    COUNT(o.id),
                    COALESCE(SUM(o.totalAmount), 0),
                    MAX(o.createdAt)
                )
                FROM CustomerOrder o
                LEFT JOIN UserAddress a
                    ON a.userMobile = COALESCE(o.customerMobile, o.userMobile)
                WHERE COALESCE(o.customerMobile, o.userMobile) IS NOT NULL
                GROUP BY COALESCE(o.customerMobile, o.userMobile)
                ORDER BY MAX(o.createdAt) DESC
            """)
    List<AdminCustomerResponse> getAdminCustomers();

    @Query("""
                SELECT o
                FROM CustomerOrder o
                WHERE o.customerMobile = :mobile OR o.userMobile = :mobile
                ORDER BY o.createdAt DESC
            """)
    List<CustomerOrder> findCustomerOrdersByMobile(@Param("mobile") String mobile);
}