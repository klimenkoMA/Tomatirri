// Сохраняем исходное изображение
const originalImageSrc = document.getElementById('mainImage').src;

function changeMainImage(thumbnail) {
    const newSrc = thumbnail.getAttribute('data-main-image-src');
    document.getElementById('mainImage').src = newSrc;
}

function resetMainImage() {
    document.getElementById('mainImage').src = originalImageSrc;
}