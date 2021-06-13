package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CouponDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.dao.OrderDao;
import com.upgrad.FoodOrderingApp.service.dao.OrderItemDao;
import com.upgrad.FoodOrderingApp.service.entity.CouponEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderEntity;
import com.upgrad.FoodOrderingApp.service.entity.OrderItemEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.CouponNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderDao orderDao;
    @Autowired
    private CouponDao couponDao;
    @Autowired
    private UtilityService utilityService;
    @Autowired
    private CustomerDao customerDao;
    @Autowired
    OrderItemDao orderItemDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public CouponEntity getCouponByCouponName(final String couponName) throws AuthorizationFailedException,CouponNotFoundException {
        if (utilityService.isStringEmptyOrNull(couponName)) {
            throw new CouponNotFoundException("CPF-002", "Coupon name field should not be empty");
        }
        CouponEntity couponEntity = couponDao.getCouponByName(couponName);
        if (couponEntity == null) {
            throw new CouponNotFoundException("CPF-001", "No coupon by this name");
        }
        return couponEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<OrderEntity> getOrdersByCustomers(String customerUuid) {
        CustomerEntity customerEntity = customerDao.getCustomerByUuid(customerUuid);
        List<OrderEntity> ordersEntities = orderDao.getOrdersByCustomers(customerEntity);
        return ordersEntities;
    }

    public List<OrderItemEntity> getOrderItemsByOrder(OrderEntity orderEntity) {
        List<OrderItemEntity> orderItemEntities = orderItemDao.getOrderItemsByOrder(orderEntity);
        return orderItemEntities;
    }
}
