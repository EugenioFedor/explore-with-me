package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.NewCompilationDto;
import ru.practicum.ewm.dto.UpdateCompilationRequest;

public interface CompilationService {
    CompilationDto addCompilation(NewCompilationDto newCompilationDto);
    void deleteCompilation(Long compId);
    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest);
}
