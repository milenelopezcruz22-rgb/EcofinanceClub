package com.ecofinance.ecofinance.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ecofinance.ecofinance.entity.Categoria;
import com.ecofinance.ecofinance.repository.CategoriaRepository;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository repository;

    public List<Categoria> listar() {
        return repository.findAll();
    }

    public Optional<Categoria> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public List<Categoria> listarGrupo(Long idGrupo) {
        return repository.listarGrupo(idGrupo);
    }

    public Categoria guardar(Categoria categoria) {
        return repository.save(categoria);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}