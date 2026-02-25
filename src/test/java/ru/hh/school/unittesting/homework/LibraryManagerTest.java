package ru.hh.school.unittesting.homework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LibraryManagerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private LibraryManager libraryManager;

    @BeforeEach
    void setUp() {
        libraryManager.addBook("book1", 11);
    }

    @Test
    void addBookShouldAddNewBookWithQuantity() {
        libraryManager.addBook("book2", 6);

        assertEquals(6, libraryManager.getAvailableCopies("book2"));
    }

    @Test
    void addBookShouldIncreaseQuantityForExistingBook() {
        libraryManager.addBook("book1", 10);

        assertEquals(21, libraryManager.getAvailableCopies("book1"));
    }

    @Test
    void borrowBookWithActiveUserShouldSucceed() {
        when(userService.isUserActive("user1")).thenReturn(true);

        boolean result = libraryManager.borrowBook("book1", "user1");

        assertTrue(result);
        assertEquals(10, libraryManager.getAvailableCopies("book1"));
        verify(notificationService).notifyUser("user1", "You have borrowed the book: " + "book1");
    }

    @Test
    void borrowBookWithInactiveUserShouldReturnFalse() {
        when(userService.isUserActive("user1")).thenReturn(false);

        boolean result = libraryManager.borrowBook("book1", "user1");

        assertFalse(result);
        verify(notificationService).notifyUser("user1", "Your account is not active.");
    }

    @Test
    void borrowBookThatDoesNotExistShouldReturnFalse() {
        when(userService.isUserActive("user1")).thenReturn(true);

        boolean result = libraryManager.borrowBook("book0", "user1");

        assertFalse(result);
    }

    @Test
    void borrowBookWithNoAvailableCopiesShouldReturnFalse() {
        when(userService.isUserActive("user1")).thenReturn(true);

        for (int i = 0; i < 11; i++) {
            libraryManager.borrowBook("book1", "user1");
        }

        boolean result = libraryManager.borrowBook("book1", "user1");

        assertFalse(result);
    }

    @Test
    void returnBookShouldSucced() {
        when(userService.isUserActive("user1")).thenReturn(true);
        libraryManager.borrowBook("book1", "user1");

        boolean result = libraryManager.returnBook("book1", "user1");

        assertTrue(result);
        assertEquals(11, libraryManager.getAvailableCopies("book1"));
        verify(notificationService).notifyUser("user1", "You have returned the book: " + "book1");
    }

    @Test
    void returnBookByAnotherUserShouldReturnFalse() {
        when(userService.isUserActive("user1")).thenReturn(true);
        libraryManager.borrowBook("book1", "user1");

        boolean result = libraryManager.returnBook("book1", "user2");

        assertFalse(result);
    }

    @Test
    void returnNotBorrowedBookShouldReturnFalse() {
        boolean result = libraryManager.returnBook("book1", "user1");

        assertFalse(result);
    }

    @Test
    void getAvailableCopiesForExistingBook() {

        assertEquals(11, libraryManager.getAvailableCopies("book1"));
    }

    @Test
    void getAvailableCopiesForNonExistingBookShouldReturnZero() {

        assertEquals(0, libraryManager.getAvailableCopies("book0"));

    }

    @ParameterizedTest
    @CsvSource({
            "1, false, false, 0.50",
            "2, true, false, 1.50",
            "5, false, true, 2.00 ",
            "3, true, true, 1.80"
    })
    void calculateDynamicLateFeeShouldReturnCorrectAmount(
            int overdueDays,
            boolean isBestseller,
            boolean isPremiumMember,
            double expectedFee) {

        double totalFee = libraryManager.calculateDynamicLateFee(overdueDays, isBestseller, isPremiumMember);

        assertEquals(expectedFee, totalFee);
    }

    @Test
    void calculateDynamicLateFeeShouldThrowExceptionIfOverdueDaysIsIncorrect() {
        var exception = assertThrows(IllegalArgumentException.class,
                () -> libraryManager.calculateDynamicLateFee(-3, false, true));

        assertEquals("Overdue days cannot be negative.", exception.getMessage());
    }

}