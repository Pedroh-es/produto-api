package com.trabalho.api.repositories;

import com.trabalho.api.entities.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IJpaProdutoRepo extends JpaRepository<Produto, Long> {
}
