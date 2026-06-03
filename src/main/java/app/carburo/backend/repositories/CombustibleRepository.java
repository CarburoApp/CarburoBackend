package app.carburo.backend.repositories;


import app.carburo.backend.entities.Combustible;
import org.springframework.data.repository.CrudRepository;

public interface CombustibleRepository extends CrudRepository<Combustible, Short> {}
