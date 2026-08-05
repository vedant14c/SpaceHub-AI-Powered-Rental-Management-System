package com.officespace.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.officespace.entities.BookingStatus;
import java.time.LocalDate;

public class BookedDateRangeDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate proposedStart;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate proposedEnd;

    private BookingStatus status;

    public BookedDateRangeDTO() {
    }

    public BookedDateRangeDTO(LocalDate proposedStart, LocalDate proposedEnd) {
        this.proposedStart = proposedStart;
        this.proposedEnd = proposedEnd;
        this.status = BookingStatus.CONFIRMED;
    }

    public BookedDateRangeDTO(LocalDate proposedStart, LocalDate proposedEnd, BookingStatus status) {
        this.proposedStart = proposedStart;
        this.proposedEnd = proposedEnd;
        this.status = status;
    }

    public LocalDate getProposedStart() {
        return proposedStart;
    }

    public void setProposedStart(LocalDate proposedStart) {
        this.proposedStart = proposedStart;
    }

    public LocalDate getProposedEnd() {
        return proposedEnd;
    }

    public void setProposedEnd(LocalDate proposedEnd) {
        this.proposedEnd = proposedEnd;
    }

    public String getStartDate() {
        return proposedStart != null ? proposedStart.toString() : null;
    }

    public String getEndDate() {
        return proposedEnd != null ? proposedEnd.toString() : null;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
