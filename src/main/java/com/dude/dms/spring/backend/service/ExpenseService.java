package com.dude.dms.spring.backend.service;

import com.dude.dms.spring.backend.data.entity.Expense;
import com.dude.dms.spring.backend.data.entity.User;
import com.dude.dms.spring.backend.repositories.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpenseService implements CrudService<Expense> {

  private final ExpenseRepository expenseRepository;

  @Autowired
  public ExpenseService(ExpenseRepository expenseRepository) {
    this.expenseRepository = expenseRepository;
  }

  @Transactional(rollbackOn = Exception.class)
  public Expense save(Expense expense, User currentUser) {
    return expenseRepository.save(expense);
  }

  public List<Expense> findAll() {
    return expenseRepository.findAll();
  }

  @Override
  public JpaRepository<Expense, Long> getRepository() {
    return expenseRepository;
  }

  public long countAnyMatchingAfterTimestamp(LocalDateTime optionalFilterTimestamp) {
    return optionalFilterTimestamp != null ? expenseRepository.countByTimestampAfter(optionalFilterTimestamp) : expenseRepository.count();
  }
}
