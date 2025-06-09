document.addEventListener('DOMContentLoaded', function () {
    var galleryModal = document.getElementById('galleryModal');
    if (galleryModal) {
        galleryModal.addEventListener('shown.bs.modal', function () {
            var startIndex = parseInt(this.getAttribute('data-start-index')) || 0;
            var carousel = new bootstrap.Carousel(document.getElementById('galleryCarousel'), {});
            carousel.to(startIndex); // Переходим к нужному слайду
        });
    }
});