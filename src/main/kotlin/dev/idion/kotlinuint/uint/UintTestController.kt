package dev.idion.kotlinuint.uint

import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RequestMapping("/uint")
@RestController
class UintTestController(
    private val uintTestRepository: UintTestRepository
) {

    @GetMapping
    @Transactional(readOnly = true)
    fun all(): List<UintTest?> {
        return uintTestRepository.findAll()
    }

    @PostMapping
    @Transactional
    fun create(@RequestParam(required = false) mau: Long?) {
        uintTestRepository.save(UintTest(mau?.toUInt()))
    }
}
