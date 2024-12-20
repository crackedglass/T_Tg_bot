package com.example.tgbotdemo.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tgbotdemo.repositories.*;

import lombok.extern.slf4j.Slf4j;

import com.example.tgbotdemo.domain.*;

@Slf4j
@Service
public class OrderService {

    private UserService userService;
    private OrderRepository orderRepository;
    private UserRepository userRepository;

    public OrderService(UserService userService, OrderRepository orderRepository, UserRepository userRepository) {
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void create(User user, Cell cell, int amount) {
        log.info("Create transaction on user \"" + user.getUsername() + "\" called Cell: " + cell.getNumber()
                + " amount: " + amount);
        Order exist = orderRepository.findByUserAndCell(user, cell);

        if (exist == null) {
            user.setMoney(user.getMoney() - amount);
            orderRepository.save(new Order(user,
                    cell,
                    amount));
        } else {
            user.setMoney(user.getMoney() - amount);
            if (amount == 0) {
                orderRepository.delete(exist);
            } else {
                exist.setAmount(exist.getAmount() + amount);
                orderRepository.save(exist);
            }

        }

        userRepository.save(user);
    }

    public List<Order> getOrdersByUsername(String username) {
        User user = userService.getByUsername(username);
        return orderRepository.findByUser(user);
    }

    @Transactional
    public void revertAll(List<Order> orders) {
        for (Order order : orders) {
            User user = userRepository.findByUsername(order.getUser().getUsername());
            int money = user.getMoney();
            user.setMoney(money + order.getAmount());
            userRepository.save(user);
        }
        orderRepository.deleteAll();
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public void deleteAll() {
        orderRepository.deleteAll();
    }

    public void save(Order order) {
        orderRepository.save(order);
    }
}
