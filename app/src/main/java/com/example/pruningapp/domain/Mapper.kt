package com.example.pruningapp.domain

// Generyczny kontrakt transformacji danych między warstwami.
// Użycie: DTO → model domenowy, encja Room → model prezentacji.
// Bezstronne od Androida — może być testowane czystymi testami jednostkowymi.
interface Mapper<I, O> {
    fun map(input: I): O
}
