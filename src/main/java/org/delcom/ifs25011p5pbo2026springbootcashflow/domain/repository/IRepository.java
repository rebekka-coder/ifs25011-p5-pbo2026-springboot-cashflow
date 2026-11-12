package org.delcom.ifs25011p5pbo2026springbootcashflow.domain.repository;

import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.exception.EntityNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/** Port generik CRUD + findBy. Implementasi: InMemory (unit test) dan JDBC (runtime). */
public interface IRepository<T, ID> {
    Optional<T> findById(ID id);

    List<T> findAll();

    List<T> findBy(Predicate<T> predicate);

    T update(T entity) throws EntityNotFoundException;

    void deleteById(ID id) throws EntityNotFoundException;
}
