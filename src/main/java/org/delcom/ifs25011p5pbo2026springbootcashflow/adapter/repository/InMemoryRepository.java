package org.delcom.ifs25011p5pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25011p5pbo2026springbootcashflow.domain.repository.IRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/** Basis generik implementasi in-memory (untuk unit test). */
public abstract class InMemoryRepository<T, ID> implements IRepository<T, ID> {
    protected final Map<ID, T> store = new LinkedHashMap<>();

    protected abstract ID idOf(T entity);

    protected abstract String entityName();

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<T> findBy(Predicate<T> predicate) {
        return store.values().stream().filter(predicate).toList();
    }

    @Override
    public T update(T entity) throws EntityNotFoundException {
        ID id = idOf(entity);
        if (!store.containsKey(id)) {
            throw new EntityNotFoundException(entityName(), id);
        }
        store.put(id, entity);
        return entity;
    }

    @Override
    public void deleteById(ID id) throws EntityNotFoundException {
        if (store.remove(id) == null) {
            throw new EntityNotFoundException(entityName(), id);
        }
    }
}
